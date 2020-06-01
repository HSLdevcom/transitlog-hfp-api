package fi.hsl.transitloghfpapi.config.enablebatchprocessing;

import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
@org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
public class EnableBatchProcessing {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

}
