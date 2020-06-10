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
    private BlobStorage blobStorage;
    private EventRepository temporaryHfpRepository;

    @Autowired
    public HfpBatchService(EventRepository temporaryHfpRepository,
                           JobLauncher jobLauncher,
                           StepBuilderFactory stepBuilderFactory,
                           PlatformTransactionManager platformTransactionManager,
                           JobBuilderFactory jobBuilderFactory,
                           JobExplorer jobExplorer,
                           BlobRepository blobRepository,
                           BlobStorage blobStorage) {
        this.blobRepository = blobRepository;
        this.jobExplorer = jobExplorer;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.blobStorage = blobStorage;
        this.temporaryHfpRepository = temporaryHfpRepository;
        this.platformTransactionmanager = platformTransactionManager;
        this.jobBuilderFactory = jobBuilderFactory;
    }


    public ResponseEntity<Long> startHfpCollectionJob(LocalDateTime startDate, LocalDateTime endDate) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        final JobExecution today = createHfpJob(startDate, endDate);
        return new ResponseEntity<>(today.getJobId(), HttpStatus.ACCEPTED);
    }

    JobExecution createHfpJob(LocalDateTime startDate, LocalDateTime endDate) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        return jobLauncher.run(hfpCollectionJob(startDate, endDate), new JobParameters(Map.of("today", new JobParameter(Calendar.getInstance().getTime()))));
    }


    private Job hfpCollectionJob(LocalDateTime start, LocalDateTime end) {
        final JobBuilder jobBuilder = jobBuilderFactory.get("hfp-collection-job");
        return jobBuilder.flow(insertRangeIntoDatabase(start, end))
                .end().build();
    }


    private Step insertRangeIntoDatabase(LocalDateTime start, LocalDateTime end) {
        return stepBuilderFactory.get("insertRangeIntoDatabaseStep")
                .transactionManager(platformTransactionmanager)
                .<Event, Event>chunk(10)
                .reader(new HfpItemBatchOperations.BlobItemReader(start, end, blobStorage, List.of(LightPriorityEvent.class, StopEvent.class, VehiclePosition.class, OtherEvent.class)))
                .processor(new HfpItemBatchOperations.FilteringProcessor())
                .writer(new HfpItemBatchOperations.EventWriter(temporaryHfpRepository))
                .faultTolerant()
                .retryPolicy(new AlwaysRetryPolicy())
                .build();

    }

    public ResponseEntity<String> getDownloadLinkIfReady(long jobExecutionId) {
        final JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
        if (jobExecution == null) {
            throw new HfpFetchException("Hfp job doesn't exist, please create hfp job");
        }
        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            return new ResponseEntity<>(blobRepository.findByJobExecutionId(jobExecutionId).getDownloadLink(), HttpStatus.OK);
        }

        throw new HfpFetchException("Hfp job not yet finished, please return later");
    }
}
