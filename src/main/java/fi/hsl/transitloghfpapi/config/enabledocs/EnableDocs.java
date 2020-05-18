package fi.hsl.transitloghfpapi.config.enabledocs;

import org.springframework.context.annotation.*;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.*;
import springfox.documentation.spring.web.plugins.*;
import springfox.documentation.swagger2.annotations.*;

import java.util.*;

@EnableSwagger2
@Configuration
public class EnableDocs {
    @Bean
    public Docket hfpApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/v1/hfp/**"))
                .build()
                .apiInfo(new ApiInfo("HFP Api", "Api for fetching public HFP data", "1.0", null, null, null, null, Collections.emptyList()));
    }
}
