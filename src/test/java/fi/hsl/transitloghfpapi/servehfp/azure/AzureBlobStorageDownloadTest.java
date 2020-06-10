package fi.hsl.transitloghfpapi.servehfp.azure;

import org.junit.jupiter.api.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AzureBlobStorageDownloadTest {

    @Test
    void filenameStrategy() {
        final AzureFileProperties.BlobStorageFilenameStrategy blobStorageFilenameStrategy = new AzureFileProperties.BlobStorageFilenameStrategy();
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime after = LocalDateTime.now();
        final LocalDateTime plus = after.plus(5, ChronoUnit.YEARS);
        final List<String> hfpFilenames = blobStorageFilenameStrategy.createAllEventsFileNames(now, plus);
        final String s = hfpFilenames.get(0);
        assertTrue(s.contains(".csv"));
        assertTrue(s.contains("LightPriorityEvent"));
    }
}