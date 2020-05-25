package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;

import java.util.*;

class HfpCSVMapper implements CsvMapper {
    @Override
    public List<Event> mapToCsv(String filePath) {
        throw new RuntimeException("Not implemented yet exception");
    }
}
