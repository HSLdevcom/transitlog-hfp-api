package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import fi.hsl.transitloghfpapi.domain.*;
import reactor.core.publisher.*;

import java.util.*;
import java.util.stream.*;

public class AzureDownload {

    private final BlobContainerAsyncClient blobContainerClient;

    public AzureDownload(String blobConnectionString, String containerName) {
        BlobServiceAsyncClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildAsyncClient();
        blobContainerClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
    }

    public List<LocalBlob> downloadBlob(List<String> listOfHfpFilenames) {
        return listOfHfpFilenames.stream()
                .map(this::downloadBlob)
                .collect(Collectors.toList());


    }

    private LocalBlob downloadBlob(String fileName) {
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

        public List<Event> createEntryList(CsvMapper csvMapper) {
            final BlobProperties block = this.blobPropertiesMono.block();

            //Read the csv file from filesystem into events
            return csvMapper.mapToCsv(filePath);

        }

        public void deleteLocalBlob() {
            throw new RuntimeException("Not implemented yet exception");
        }
    }
}
