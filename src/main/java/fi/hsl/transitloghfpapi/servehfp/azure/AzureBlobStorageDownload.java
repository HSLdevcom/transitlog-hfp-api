package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import fi.hsl.transitloghfpapi.domain.*;
import org.springframework.stereotype.*;
import reactor.core.publisher.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.*;

public class AzureBlobStorageDownload {

    private final BlobContainerAsyncClient blobContainerClient;

    public AzureBlobStorageDownload(String blobConnectionString, String containerName) {
        BlobServiceAsyncClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildAsyncClient();
        blobContainerClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
    }

    public List<Event> downloadblob(Date start, Date end) {
        return
                downloadblob(new BlobStorageFilenameStrategy().createHfpFilenames(start, end));
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

        public void deleteLocalBlob() {
            throw new RuntimeException("Not implemented yet exception");
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
        List<String> createHfpFilenames(Date startDate, Date endDate) {
            throw new RuntimeException("Not yet implemented");
        }
    }
}
