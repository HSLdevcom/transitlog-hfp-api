package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import lombok.extern.slf4j.*;
import org.junit.Test;
import org.junit.jupiter.api.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit4.*;

import java.util.concurrent.*;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest
@Slf4j
class ByteToEventStreamTest {
    @Autowired
    private BlobStorage blobStorage;

    private ByteToEventStream byteToEventStream;

    @BeforeEach
    void setUp() {
        this.byteToEventStream = new ByteToEventStream(StopEvent.class);
    }

    @Test
    void readEvent() {
        final CompletableFuture<Boolean> booleanCompletableFuture = this.blobStorage.downloadAsync("csv/StopEvent/2020-05-03-15.csv", this.byteToEventStream);
        await()
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> {
                    final Event event = byteToEventStream.readEvent();
                    return event instanceof StopEvent;
                });
        assertFalse(booleanCompletableFuture.isDone());
    }
}