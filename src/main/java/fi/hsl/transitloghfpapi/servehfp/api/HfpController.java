package fi.hsl.transitloghfpapi.servehfp.api;

import fi.hsl.transitloghfpapi.servehfp.batch.*;
import io.swagger.annotations.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.format.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.*;

@RestController
@RequestMapping("/v1/hfp")
@Api(tags = {"HFP download"}, value = "asdf")
public class HfpController {

    @Autowired
    private HfpBatchService hfpBatchService;

    @PostMapping("/collectHfp")
    @ApiResponse(message = "Returns a token representing a batch job id for HFP collection", code = 202)
    @ApiOperation(value = "Returns a token representing a batch job id for HFP collection")
    public ResponseEntity<Long> registerJob(@ApiParam(value = "Start date in a format YYYY-mm-dd", required = true, name = "startDate")
                                            @RequestParam(value = "startDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @ApiParam(value = "End date in a format yyyy-MM-dd", required = true, name = "endDate")
                                            @RequestParam(value = "endDate", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        if (endDate.isBefore(startDate)) {
            throw new HfpFetchException("Enddate cannot be before start date");
        }
        return hfpBatchService.createHFPCollectionJob(startDate.atStartOfDay(), endDate.plus(1, ChronoUnit.DAYS).atStartOfDay());
    }

    @GetMapping("/downloadBlob/{blobid}")
    @ApiResponses({
            @ApiResponse(message = "Returns a download link for the blob if it's already ready", code = 200),
            @ApiResponse(message = "Returns a 404 because download link is not ready yet or it doesn't exist", code = 404)
    })
    public ResponseEntity<String> getDownloadLink(@PathVariable(value = "blobid") long blobid) {
        return hfpBatchService.getDownloadLinkIfReady(blobid);
    }

}
