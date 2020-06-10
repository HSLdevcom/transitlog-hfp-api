package fi.hsl.transitloghfpapi.servehfp.azure;


import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit4.*;

import java.io.*;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest
@Slf4j
public class BlobStorageTest {

    @Autowired
    private BlobStorage blobStorage;

    @Test
    public void testAsyncBlobStreaming() throws ExecutionException, InterruptedException {
        TestCharacterConsumerOutputStream outputStream = new TestCharacterConsumerOutputStream();

        final CompletableFuture<Boolean> downloadReady = blobStorage.downloadAsync("csv/StopEvent/2020-05-01-15.csv", outputStream);
        await()
                .atMost(10, TimeUnit.MINUTES)
                .until(outputStream::isReceivedNewLine);
        assertFalse(downloadReady.isDone());
    }

    @Data
    private class TestCharacterConsumerOutputStream extends ByteToEventStream {
        private final StringBuilder characterConsumerStringBuilder;
        private boolean receivedNewLine;

        TestCharacterConsumerOutputStream() {
            super(StopEvent.class);
            this.characterConsumerStringBuilder = new StringBuilder();
            this.receivedNewLine = false;
        }

        @Override
        public void write(int i) throws IOException {
            final char character = (char) i;
            characterConsumerStringBuilder.append(character);
            if (character == '\n') {
                log.info(characterConsumerStringBuilder.toString());
                receivedNewLine = true;
            }
        }
    }
}
