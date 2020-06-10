package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit4.*;

import java.time.*;
import java.util.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest
@Slf4j
class HfpDownloadTest {

    private HfpDownload hfpDownload;

    @Autowired
    private BlobStorage blobStorage;

    @BeforeEach
    void setUp() {
        var yesterday = LocalDateTime.now().minusDays(1);
        var daybefore = LocalDateTime.now().minusDays(2);
        this.hfpDownload = new HfpDownload(daybefore, yesterday, blobStorage, List.of(StopEvent.class, LightPriorityEvent.class));
    }

    @Test
    void streamNextEvent() {
        //Get a couple of events
        final Event event = this.hfpDownload.streamNextEvent();
        final Event event1 = this.hfpDownload.streamNextEvent();


    }
}