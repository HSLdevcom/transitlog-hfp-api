package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.*;
import reactor.core.publisher.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class BlobStorage {

    private final BlobContainerAsyncClient blobContainerClient;
    private final ExecutorService executors;

    public BlobStorage(@Value("${blobConnectionString}") String blobConnectionString, @Value("${containerName}") String containerName) {
        this.executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        BlobServiceAsyncClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildAsyncClient();
        blobContainerClient = blobServiceClient.getBlobContainerAsyncClient(containerName);
    }

    public CompletableFuture<Boolean> downloadAsync(String filePath, ByteToEventStream outputStream) {
        CompletableFuture<Boolean> streamingFinished = new CompletableFuture<>();
        final Flux<ByteBuffer> download = download(filePath);
        final WritableByteChannel writableByteChannel = Channels.newChannel(outputStream);
        subscribeToAsyncDownload(download, writableByteChannel);
        download.doOnComplete(() -> {
            streamingFinished.complete(true);
            outputStream.setDownloading(false);
        });
        return streamingFinished;
    }

    private void subscribeToAsyncDownload(Flux<ByteBuffer> download, WritableByteChannel writableByteChannel) {
        download.subscribe(byteBuffer -> {
            try {
                writableByteChannel.write(byteBuffer);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write bytebuffer", e);
            }
        });
    }

    private Flux<ByteBuffer> download(String filePath) {
        final BlobAsyncClient blobAsyncClient = blobContainerClient.getBlobAsyncClient(filePath);
        return blobAsyncClient
                .download();
    }

    private LocalBlob downloadBlob(AzureFileProperties azureFileProperties) {
        try {
            return getByteBuffers(azureFileProperties);
        } catch (IOException e) {
            log.debug("Failed to download filename: {}", azureFileProperties.getFilePath());
            return null;
        }
    }


    private LocalBlob getByteBuffers(AzureFileProperties fileProperties) throws IOException {
        final BlobAsyncClient blobClient;
        try {
            blobClient = blobContainerClient.getBlobAsyncClient(fileProperties.getFilePath());
        } catch (Exception e) {
            throw new IOException(e);
        }
        try {
            return new LocalBlob(blobClient, fileProperties);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public class LocalBlob {
        private AzureFileProperties fileProperties;
        private BlobAsyncClient blockBlobClient;

        LocalBlob(BlobAsyncClient blockClient, AzureFileProperties fileProperties) {
        }

        ByteToEventStream getByteConsumer() throws IOException {
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
            final ByteToEventStream byteToEventStream = new ByteToEventStream(hfpClass);
            return byteToEventStream;
        }

    }
}

