package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import fi.hsl.transitloghfpapi.domain.*;
import org.springframework.stereotype.*;
import reactor.core.publisher.*;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

public class AzureBlobStorageDownload {

    private final BlobContainerAsyncClient blobContainerClient;

    public AzureBlobStorageDownload(String blobConnectionString, String containerName) {
        BlobServiceAsyncClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildAsyncClient();
        blobContainerClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
    }

    public List<Event> downloadblob(LocalDateTime start, LocalDateTime end) {
        return
                downloadblob(new BlobStorageFilenameStrategy().createAllEventsFileNames(start, end));
    }

    private List<Event> downloadblob(List<String> listOfHfpFilenames) {
        return listOfHfpFilenames.stream()
                .map(this::downloadblob)
                .flatMap(localBlob -> localBlob.createEntryList().stream())
                .collect(Collectors.toList());
    }


    private LocalBlob downloadblob(String fileName) {
        final BlobAsyncClient blobClient = blobContainerClient.getBlobAsyncClient(fileName);
        final Mono<BlobProperties> blobPropertiesMono = blobClient.downloadToFile(fileName);
        return new LocalBlob(blobPropertiesMono, fileName);
    }

    public static class LocalBlob {
        private final String filePath;
        private final Mono<BlobProperties> blobPropertiesMono;

        LocalBlob(Mono<BlobProperties> blobPropertiesMono, String fileName) {
            this.blobPropertiesMono = blobPropertiesMono;
            this.filePath = fileName;
        }

        List<Event> createEntryList() {
            final BlobProperties block = this.blobPropertiesMono.block();

            //Read the csv file from filesystem into events
            return new BlobStorageCSVFormatMapper().mapCsvToEvent(filePath);

        }
    }

    static class BlobStorageCSVFormatMapper implements CsvMapper {
        @Override
        public List<Event> mapCsvToEvent(String filePath) {
            throw new RuntimeException("Not implemented yet exception");
        }
    }

    @Component
    static
    class BlobStorageFilenameStrategy {
        List<String> createAllEventsFileNames(LocalDateTime startDate, LocalDateTime endDate) {//Split to hourly ranges
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

            hfpFileNames.add(HFPFilenamePrefix.LIGHTPRIORITYEVENT.filename + properDateFormat + ".csv");
            hfpFileNames.add(HFPFilenamePrefix.OTHEREVENT + properDateFormat + ".csv");
            hfpFileNames.add(HFPFilenamePrefix.STOPEVENT + properDateFormat + ".csv");
            hfpFileNames.add(HFPFilenamePrefix.VEHICLEPOSITION + properDateFormat + ".csv");

            return hfpFileNames;
        }


        private enum HFPFilenamePrefix {
            LIGHTPRIORITYEVENT("/csv/LightPriorityEvent/"),
            OTHEREVENT("/csv/OtherEvent/"),
            STOPEVENT("/csv/StopEvent"),
            VEHICLEPOSITION("/csv/VehiclePosition");

            private final String filename;

            HFPFilenamePrefix(String filename) {
                this.filename = filename;
            }
        }
    }
}

