package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import fi.hsl.transitloghfpapi.domain.repositories.*;
import fi.hsl.transitloghfpapi.servehfp.azure.*;
import org.springframework.batch.item.*;

import java.sql.Date;
import java.util.*;

class HfpBatchOperations {

    public static class PassThroughProcessor implements ItemProcessor<List<Event>, List<Event>> {

        @Override
        public List<Event> process(List<Event> events) throws Exception {
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
        private final Date start;
        private final Date end;
        private final AzureBlobStorageDownload azureBlobStorageDownload;

        AzureItemReader(Date start, Date end, AzureBlobStorageDownload azureBlobStorageDownload) {
            this.start = start;
            this.end = end;
            this.azureBlobStorageDownload = azureBlobStorageDownload;
        }

        @Override
        public List<Event> read() throws Exception {
            return azureBlobStorageDownload.downloadblob(start, end);
        }

    }

}
