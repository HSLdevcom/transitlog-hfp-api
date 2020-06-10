package fi.hsl.transitloghfpapi.servehfp.batch;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import lombok.extern.slf4j.*;
import org.springframework.batch.item.*;

import java.time.*;
import java.util.*;

class HfpItemBatchOperations {

    public static class FilteringProcessor implements ItemProcessor<Event, Event> {


        @Override
        public Event process(Event event) throws Exception {
            return event;
        }
    }

    @Slf4j
    public static class EventWriter implements ItemWriter<Event> {

        private final EventRepository temporaryRepository;

        EventWriter(EventRepository temporaryRepository) {
            this.temporaryRepository = temporaryRepository;

        }

        @Override
        public void write(List<? extends Event> list) throws Exception {
            log.info("Saving {} entities", list.size());
            this.temporaryRepository.saveAll(list);
        }
    }

    public static class BlobItemReader implements ItemReader<Event> {
        private final HfpDownload hfpDownload;

        BlobItemReader(LocalDateTime start, LocalDateTime end, BlobStorage blobStorage, List<Class<? extends Event>> typesToFetch) {
            this.hfpDownload = new HfpDownload(start, end, blobStorage, typesToFetch);
        }

        @Override
        public Event read() {
            return hfpDownload.streamNextEvent();

        }

    }

}
