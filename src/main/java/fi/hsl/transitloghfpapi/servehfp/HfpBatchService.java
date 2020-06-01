package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.*;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.*;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.*;

import java.sql.Date;
import java.util.*;

@Service
class HfpBatchService {
    private final JobLauncher jobLauncher;
    private final StepBuilderFactory stepBuilderFactory;
    private final PlatformTransactionManager platformTransactionmanager;
    private final JobBuilderFactory jobBuilderFactory;
    private AzureBlobStorageDownload azureBlobStorageDownload;
    private EventRepository hfpRepository;

    @Autowired
    public HfpBatchService(@Value("${blobConnectionString}") String blobConnectionString, @Value("${containerName}") String containerName, EventRepository hfpRepository,
                           JobLauncher jobLauncher,
                           StepBuilderFactory stepBuilderFactory,
                           PlatformTransactionManager platformTransactionManager,
                           JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.azureBlobStorageDownload = new AzureBlobStorageDownload(blobConnectionString, containerName);
        this.hfpRepository = hfpRepository;
        this.platformTransactionmanager = platformTransactionManager;
        this.jobBuilderFactory = jobBuilderFactory;
    }


    ResponseEntity<Long> createHFPCollectionJob(Date startDate, Date endDate) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        final JobExecution today = jobLauncher.run(hfpCollectionJob(startDate, endDate), new JobParameters(Map.of("today", new JobParameter(Calendar.getInstance().getTime()))));
        return new ResponseEntity<>(today.getJobId(), HttpStatus.ACCEPTED);
    }

    private Job hfpCollectionJob(Date start, Date end) {
        final JobBuilder jobBuilder = jobBuilderFactory.get("hfp-collection-job");
        return jobBuilder.flow(insertRangeIntoDatabase(start, end))
                .end().build();
    }

    private Step insertRangeIntoDatabase(Date start, Date end) {
        return stepBuilderFactory.get("insertRangeIntoDatabaseStep")
                .transactionManager(platformTransactionmanager)
                .<List<Event>, List<Event>>chunk(1000)
                .reader(azureReader(start, end))
                .processor(new HfpBatchOperations.PassThroughProcessor())
                .writer(temporaryDatabaseWriter()).build();

    }

    private ItemWriter<? super List<Event>> temporaryDatabaseWriter() {
        return new HfpBatchOperations.AzureItemWriter(hfpRepository);
    }

    private ItemReader<List<Event>> azureReader(Date start, Date end) {
        return new HfpBatchOperations.AzureItemReader(start, end, azureBlobStorageDownload);
    }
}
