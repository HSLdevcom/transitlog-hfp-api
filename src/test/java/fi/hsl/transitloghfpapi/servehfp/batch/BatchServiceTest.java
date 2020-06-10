package fi.hsl.transitloghfpapi.servehfp.batch;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.test.context.support.*;

import java.time.*;
import java.util.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
public class BatchServiceTest {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private HfpBatchService hfpBatchService;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("job_started", String.valueOf(new Date()));
        return paramsBuilder.toJobParameters();
    }

    @Test
    public void testHfpDownloadJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        final LocalDateTime twodaysago = LocalDateTime.now().minusDays(2);
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        final JobExecution hfpCollectionJob = hfpBatchService.createHfpJob(twodaysago, yesterday);
    }

}