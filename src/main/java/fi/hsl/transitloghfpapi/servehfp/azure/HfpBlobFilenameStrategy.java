package fi.hsl.transitloghfpapi.servehfp.azure;

import org.springframework.stereotype.*;

import java.sql.Date;
import java.util.*;

@Component
public class HfpBlobFilenameStrategy {
    public List<String> createHfpFilenames(Date startDate, Date endDate) {
        throw new RuntimeException("Not yet implemented");
    }
}
