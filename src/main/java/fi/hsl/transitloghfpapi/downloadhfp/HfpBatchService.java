package fi.hsl.transitloghfpapi.downloadhfp;

import org.springframework.http.*;
import org.springframework.stereotype.*;

import java.sql.*;

@Service
class HfpBatchService {
    ResponseEntity<String> createHFPCollectionJob(Date startDate, Date endDate) {
        return new ResponseEntity<>("Job accepted", HttpStatus.ACCEPTED);
    }
}
