package fi.hsl.transitloghfpapi.servehfp.azure;

import lombok.*;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

@Data
class AzureFileProperties {
    private final String filePath;
    private BlobStorageFilenameStrategy.HFPFilenamePrefix eventType;

    private AzureFileProperties(String filePath, BlobStorageFilenameStrategy.HFPFilenamePrefix eventType) {
        this.eventType = eventType;
        this.filePath = filePath;
    }

    static List<AzureFileProperties> getHfpFileProperties(LocalDateTime start, LocalDateTime end) {
        final List<String> allEventsFileNames = new BlobStorageFilenameStrategy().createAllEventsFileNames(start, end);
        return allEventsFileNames
                .stream()
                .map(filename -> new AzureFileProperties(filename, getPrefix(filename)))
                .collect(Collectors.toList());
    }

    private static BlobStorageFilenameStrategy.HFPFilenamePrefix getPrefix(String filename) {
        if (filename.contains("VehiclePosition")) {
            return BlobStorageFilenameStrategy.HFPFilenamePrefix.VEHICLEPOSITION;
        }
        if (filename.contains("LightPriorityEvent")) {
            return BlobStorageFilenameStrategy.HFPFilenamePrefix.LIGHTPRIORITYEVENT;
        }
        if (filename.contains("StopEvent")) {
            return BlobStorageFilenameStrategy.HFPFilenamePrefix.STOPEVENT;
        }
        if (filename.contains("OtherEvent")) {
            return BlobStorageFilenameStrategy.HFPFilenamePrefix.OTHEREVENT;
        }
        throw new RuntimeException("HFP File type not known");
    }

    static
    class BlobStorageFilenameStrategy {

        static final String CSV = "csv/";

        List<String> createAllEventsFileNames(LocalDateTime startDate, LocalDateTime endDate) {
            //Split to hourly ranges
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
            List<LocalDateTime> localDates = new ArrayList<>();

            while (startDate.isBefore(endDate)) {
                localDates.add(startDate);
                startDate = startDate.plus(1, ChronoUnit.HOURS);
            }
            return localDates.stream()
                    .flatMap(localDateTime -> createHfpFileName(localDateTime, dateTimeFormatter).stream())
                    .collect(Collectors.toList());
        }

        private List<String> createHfpFileName(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
            List<String> hfpFileNames = new ArrayList<>();
            final String properDateFormat = localDateTime.format(dateTimeFormatter);

            hfpFileNames.add(CSV + HFPFilenamePrefix.LIGHTPRIORITYEVENT.filename + properDateFormat + ".csv");
            hfpFileNames.add(CSV + HFPFilenamePrefix.OTHEREVENT.filename + properDateFormat + ".csv");
            hfpFileNames.add(CSV + HFPFilenamePrefix.STOPEVENT.filename + properDateFormat + ".csv");
            hfpFileNames.add(CSV + HFPFilenamePrefix.VEHICLEPOSITION.filename + properDateFormat + ".csv");

            return hfpFileNames;
        }


        enum HFPFilenamePrefix {
            LIGHTPRIORITYEVENT("LightPriorityEvent/"),
            OTHEREVENT("OtherEvent/"),
            STOPEVENT("StopEvent/"),
            VEHICLEPOSITION("VehiclePosition/");

            private final String filename;

            HFPFilenamePrefix(String filename) {
                this.filename = filename;
            }
        }

    }
}
