package fi.hsl.transitloghfpapi.downloadhfp;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
@RequestMapping("/v1/hfp")
@Api(tags = {"HFP download"}, value = "asdf")
public class HfpController {

    @Autowired
    private HfpBatchService hfpBatchService;

    @PostMapping("/collectHfp")
    @ApiResponse(message = "Returns a token representing a batch job id for HFP collection", code = 202)
    @ApiOperation(value = "Returns a token representing a batch job id for HFP collection")
    public ResponseEntity<String> registerJob(@ApiParam(value = "Start date in a format YYYY-mm-dd (optional: HH)", required = true, name = "startDate")
                              @RequestParam(value = "startDate", required = true) Date startDate, @ApiParam(value = "End date in a format YYYY-mm-dd (optional: HH)", required = true, name = "endDate")
                              @RequestParam(value = "endDate", required = true) Date endDate) {
        return hfpBatchService.createHFPCollectionJob(startDate, endDate);
    }
}
