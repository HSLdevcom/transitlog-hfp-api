package fi.hsl.transitloghfpapi.servehfp.batch;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.batch.item.*;

import java.time.*;
import java.util.*;

class HfpItemBatchOperations {

    public static class PassThroughProcessor implements ItemProcessor<List<Event>, List<Event>> {

        @Override
        public List<Event> process(List<Event> events) {
            return events;
        }
    }

    public static class AzureItemWriter implements ItemWriter<List<Event>> {

        private final EventRepository temporaryRepository;

        AzureItemWriter(EventRepository temporaryRepository) {
            this.temporaryRepository = temporaryRepository;

        }


        @Override
        public void write(List<? extends List<Event>> list) throws Exception {
            list
                    .forEach(temporaryRepository::saveAll);

        }
    }

    public static class AzureItemReader implements ItemReader<List<Event>> {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final AzureBlobStorageDownload azureBlobStorageDownload;

        AzureItemReader(LocalDateTime start, LocalDateTime end, AzureBlobStorageDownload azureBlobStorageDownload) {
            this.start = start;
            this.end = end;
            this.azureBlobStorageDownload = azureBlobStorageDownload;
        }

        @Override
        public List<Event> read() {
            return azureBlobStorageDownload.downloadblob(start, end);
        }

    }

}
