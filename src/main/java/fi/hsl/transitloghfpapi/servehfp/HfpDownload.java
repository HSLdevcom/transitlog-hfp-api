package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.javatuples.*;

import java.time.*;
import java.util.*;

public class HfpDownload {
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final List<Pair<String, ByteToEventStream>> hfpStream;
    private BlobStorage blobStorage;

    public HfpDownload(LocalDateTime start, LocalDateTime end, BlobStorage blobStorage, List<Class<? extends Event>> hfpType) {
        this.start = start;
        this.end = end;
        this.blobStorage = blobStorage;
        hfpStream = HfpByteStreamFactory.createHfpStream(start, end, hfpType);
        hfpStream
                .forEach(pair -> {
                    blobStorage.downloadAsync(pair.getValue0(), pair.getValue1());
                });
    }

    public Event streamNextEvent() {
        return hfpStream
                .stream()
                .map(Pair::getValue1)
                .filter(ByteToEventStream::streamOpen)
                .map(ByteToEventStream::readEvent)
                .findFirst().orElse(null);
    }
}
