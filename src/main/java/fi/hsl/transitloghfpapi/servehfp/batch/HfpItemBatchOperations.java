package fi.hsl.transitloghfpapi.servehfp.batch;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.batch.item.*;

import java.time.*;
import java.util.*;

class HfpItemBatchOperations {

    public static class PassThroughProcessor implements ItemProcessor<Event, Event> {


        @Override
        public Event process(Event event) throws Exception {
            return event;
        }
    }

    public static class AzureItemWriter implements ItemWriter<Event> {

        private final EventRepository temporaryRepository;

        AzureItemWriter(EventRepository temporaryRepository) {
            this.temporaryRepository = temporaryRepository;

        }


        @Override
        public void write(List<? extends Event> list) throws Exception {
            this.temporaryRepository.saveAll(list);
        }
    }

    public static class AzureItemReader implements ItemReader<Event> {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final AzureBlobStorageDownload azureBlobStorageDownload;
        private final List<AzureEventConsumer> bytebuffers;

        AzureItemReader(LocalDateTime start, LocalDateTime end, AzureBlobStorageDownload azureBlobStorageDownload) {
            this.start = start;
            this.end = end;
            this.azureBlobStorageDownload = azureBlobStorageDownload;
            bytebuffers = azureBlobStorageDownload.downloadblobs(start, end);
        }

        @Override
        public Event read() {
            return bytebuffers.stream()
                    .findFirst().orElseThrow(() -> new RuntimeException("No bytebuffer present"))
                    .readEvent();

        }

    }

}
