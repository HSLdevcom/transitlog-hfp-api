package fi.hsl.transitloghfpapi.servehfp.azure;

import fi.hsl.transitloghfpapi.domain.*;

import java.util.*;

public interface CsvMapper {
    List<Event> mapCsvToEvent(String filePath);
}
