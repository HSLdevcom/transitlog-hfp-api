package fi.hsl.transitloghfpapi.config.enablebatchprocessing;

import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.launch.support.*;
import org.springframework.batch.core.repository.*;
import org.springframework.batch.core.repository.support.*;
import org.springframework.context.annotation.*;
import org.springframework.core.task.*;
import org.springframework.transaction.*;

import javax.sql.*;

@Configuration
public class EnableBatchProcessing {

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.afterPropertiesSet();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(datasource());
        factory.setDatabaseType("db2");
        factory.setTransactionManager(transactionManager());
        return factory.getObject();
    }

    private PlatformTransactionManager transactionManager() {
        return null;
    }

    private DataSource datasource() {
        return null;
    }

}
