package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import fi.hsl.transitloghfpapi.domain.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Slf4j
public class AzureBlobStorageDownload {

    private final BlobContainerClient blobContainerClient;

    public AzureBlobStorageDownload(String blobConnectionString, String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public List<AzureEventConsumer> downloadblobs(LocalDateTime start, LocalDateTime end) {
        final List<AzureFileProperties> azureFileProperties = AzureFileProperties.getHfpFiles(start, end);
        return
                downloadblobs(azureFileProperties);
    }

    private List<AzureEventConsumer> downloadblobs(List<AzureFileProperties> azureFileProperties) {
        return azureFileProperties.stream()
                .map(this::downloadBlob).filter(Objects::nonNull)
                .map(nonNullblob -> {
                    try {
                        return nonNullblob.getByteConsumer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    private LocalBlob downloadBlob(AzureFileProperties azureFileProperties) {
        try {
            return downloadblobs(azureFileProperties);
        } catch (IOException e) {
            log.debug("Failed to download filename: {}", azureFileProperties.getFilePath());
            return null;
        }
    }


    private LocalBlob downloadblobs(AzureFileProperties fileProperties) throws IOException {
        final BlobClient blobClient;
        try {
            blobClient = blobContainerClient.getBlobClient(fileProperties.getFilePath());
        } catch (Exception e) {
            throw new IOException(e);
        }
        try {
            return new LocalBlob(blobClient, fileProperties);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static class LocalBlob {
        private final AzureFileProperties fileProperties;
        private final BlobClient blockBlobClient;

        LocalBlob(BlobClient blockClient, AzureFileProperties fileProperties) {
            if (blockClient.exists()) {
                this.blockBlobClient = blockClient;
                this.fileProperties = fileProperties;
            } else {
                throw new IllegalArgumentException("Block doesnt exist");
            }
        }

        AzureEventConsumer getByteConsumer() throws IOException {
            Class<? extends Event> hfpClass = null;
            if (fileProperties.getEventType().equals(AzureFileProperties.BlobStorageFilenameStrategy.HFPFilenamePrefix.VEHICLEPOSITION)) {
                hfpClass = VehiclePosition.class;
            }
            if (fileProperties.getEventType().equals(AzureFileProperties.BlobStorageFilenameStrategy.HFPFilenamePrefix.OTHEREVENT)) {
                hfpClass = OtherEvent.class;
            }

            if (fileProperties.getEventType().equals(AzureFileProperties.BlobStorageFilenameStrategy.HFPFilenamePrefix.LIGHTPRIORITYEVENT)) {
                hfpClass = LightPriorityEvent.class;

            }

            if (fileProperties.getEventType().equals(AzureFileProperties.BlobStorageFilenameStrategy.HFPFilenamePrefix.STOPEVENT)) {
                hfpClass = StopEvent.class;
            }
            final AzureEventConsumer azureEventConsumer = new AzureEventConsumer(hfpClass);
            blockBlobClient.download(azureEventConsumer);
            return azureEventConsumer;
        }

    }
}

