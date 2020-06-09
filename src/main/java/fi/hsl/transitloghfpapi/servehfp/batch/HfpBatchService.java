package fi.hsl.transitloghfpapi.servehfp.batch;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.api.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.explore.*;
import org.springframework.batch.core.job.builder.*;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.*;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.retry.policy.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.*;

import java.time.*;
import java.util.*;

@Service
public
class HfpBatchService {
    private final JobLauncher jobLauncher;
    private final StepBuilderFactory stepBuilderFactory;
    private final PlatformTransactionManager platformTransactionmanager;
    private final JobBuilderFactory jobBuilderFactory;
    private final JobExplorer jobExplorer;
    private final BlobRepository blobRepository;
    private AzureBlobStorageDownload azureBlobStorageDownload;
    private EventRepository hfpRepository;

    @Autowired
    public HfpBatchService(@Value("${blobConnectionString}") String blobConnectionString, @Value("${containerName}") String containerName, EventRepository hfpRepository,
                           JobLauncher jobLauncher,
                           StepBuilderFactory stepBuilderFactory,
                           PlatformTransactionManager platformTransactionManager,
                           JobBuilderFactory jobBuilderFactory,
                           JobExplorer jobExplorer,
                           BlobRepository blobRepository) {
        this.blobRepository = blobRepository;
        this.jobExplorer = jobExplorer;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.azureBlobStorageDownload = new AzureBlobStorageDownload(blobConnectionString, containerName);
        this.hfpRepository = hfpRepository;
        this.platformTransactionmanager = platformTransactionManager;
        this.jobBuilderFactory = jobBuilderFactory;
    }


    public ResponseEntity<Long> createHFPCollectionJob(LocalDateTime startDate, LocalDateTime endDate) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        final JobExecution today = jobLauncher.run(hfpCollectionJob(startDate, endDate), new JobParameters(Map.of("today", new JobParameter(Calendar.getInstance().getTime()))));
        return new ResponseEntity<>(today.getJobId(), HttpStatus.ACCEPTED);
    }

    private Job hfpCollectionJob(LocalDateTime start, LocalDateTime end) {
        final JobBuilder jobBuilder = jobBuilderFactory.get("hfp-collection-job");
        return jobBuilder.flow(insertRangeIntoDatabase(start, end))
                .end().build();
    }

    private Step insertRangeIntoDatabase(LocalDateTime start, LocalDateTime end) {
        return stepBuilderFactory.get("insertRangeIntoDatabaseStep")
                .transactionManager(platformTransactionmanager)
                .<List<Event>, List<Event>>chunk(1000)
                .reader(azureReader(start, end))
                .processor(new HfpItemBatchOperations.PassThroughProcessor())
                .writer(temporaryDatabaseWriter())
                .faultTolerant()
                .retryPolicy(new AlwaysRetryPolicy())
                .build();

    }

    private ItemWriter<? super List<Event>> temporaryDatabaseWriter() {
        return new HfpItemBatchOperations.AzureItemWriter(hfpRepository);
    }

    private ItemReader<List<Event>> azureReader(LocalDateTime start, LocalDateTime end) {
        return new HfpItemBatchOperations.AzureItemReader(start, end, azureBlobStorageDownload);
    }

    public ResponseEntity<String> getDownloadLinkIfReady(Long jobExecutionId) {
        final JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution == null) {
            throw new HfpJobNotFinishedException("Hfp job doesn't exist, please create hfp job");
        }
        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            return new ResponseEntity<>(blobRepository.findByJobExecutionId(jobExecutionId).getDownloadLink(), HttpStatus.OK);
        }

        throw new HfpJobNotFinishedException("Hfp job not yet finished, please return later");
    }
}
