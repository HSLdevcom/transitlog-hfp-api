package fi.hsl.transitloghfpapi.servehfp.azure;

import com.azure.storage.blob.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.*;
import fi.hsl.transitloghfpapi.domain.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Slf4j
public class HFPBlobStorageDownloader {

    private final BlobContainerClient blobContainerClient;

    public HFPBlobStorageDownloader(String blobConnectionString, String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(blobConnectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public List<AzureByteConsumer> getBlobByteBuffers(LocalDateTime start, LocalDateTime end) {
        final List<AzureFileProperty> azureFileProperties = AzureFileProperty.getRemoteHfpFileProperties(start, end);
        return azureFileProperties.stream()
                .map(AzureByteConsumer::new)
                .collect(Collectors.toList());
    }

    @Data
    static
    class AzureFileProperty {
        private final String filePath;
        private BlobStorageFilenameStrategy.HFPFilenamePrefix eventType;

        private AzureFileProperty(String filePath, BlobStorageFilenameStrategy.HFPFilenamePrefix eventType) {
            this.eventType = eventType;
            this.filePath = filePath;
        }

        static List<AzureFileProperty> getRemoteHfpFileProperties(LocalDateTime start, LocalDateTime end) {
            final List<String> allEventsFileNames = new BlobStorageFilenameStrategy().createAllEventsFileNames(start, end);
            return allEventsFileNames
                    .stream()
                    .map(filename -> new AzureFileProperty(filename, getPrefix(filename)))
                    .collect(Collectors.toList());
        }

        private static BlobStorageFilenameStrategy.HFPFilenamePrefix getPrefix(String filename) {
            if (filename.contains("VehiclePosition")) {
                return BlobStorageFilenameStrategy.HFPFilenamePrefix.VEHICLEPOSITION;
            }
            if (filename.contains("LightPriorityEvent")) {
                return BlobStorageFilenameStrategy.HFPFilenamePrefix.LIGHTPRIORITYEVENT;
            }
            if (filename.contains("StopEvent")) {
                return BlobStorageFilenameStrategy.HFPFilenamePrefix.STOPEVENT;
            }
            if (filename.contains("OtherEvent")) {
                return BlobStorageFilenameStrategy.HFPFilenamePrefix.OTHEREVENT;
            }
            throw new RuntimeException("HFP File type not known");
        }

        static
        class BlobStorageFilenameStrategy {

            static final String CSV = "csv/";

            List<String> createAllEventsFileNames(LocalDateTime startDate, LocalDateTime endDate) {
                //Split to hourly ranges
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
                List<LocalDateTime> localDates = new ArrayList<>();

                while (startDate.isBefore(endDate)) {
                    localDates.add(startDate);
                    startDate = startDate.plus(1, ChronoUnit.HOURS);
                }
                return localDates.stream()
                        .flatMap(localDateTime -> createHfpFileName(localDateTime, dateTimeFormatter).stream())
                        .collect(Collectors.toList());
            }

            private List<String> createHfpFileName(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
                List<String> hfpFileNames = new ArrayList<>();
                final String properDateFormat = localDateTime.format(dateTimeFormatter);

                hfpFileNames.add(CSV + HFPFilenamePrefix.LIGHTPRIORITYEVENT.filename + properDateFormat + ".csv");
                hfpFileNames.add(CSV + HFPFilenamePrefix.OTHEREVENT.filename + properDateFormat + ".csv");
                hfpFileNames.add(CSV + HFPFilenamePrefix.STOPEVENT.filename + properDateFormat + ".csv");
                hfpFileNames.add(CSV + HFPFilenamePrefix.VEHICLEPOSITION.filename + properDateFormat + ".csv");

                return hfpFileNames;
            }


            enum HFPFilenamePrefix {
                LIGHTPRIORITYEVENT("LightPriorityEvent/"),
                OTHEREVENT("OtherEvent/"),
                STOPEVENT("StopEvent/"),
                VEHICLEPOSITION("VehiclePosition/");

                private final String filename;

                HFPFilenamePrefix(String filename) {
                    this.filename = filename;
                }
            }

        }
    }

    public class AzureByteConsumer extends OutputStream {
        private final com.fasterxml.jackson.dataformat.csv.CsvMapper schemaMapper;
        private Class<? extends Event> hfpType;
        private StringBuilder builder = new StringBuilder();
        private List<Event> events = new CopyOnWriteArrayList<>();

        AzureByteConsumer(AzureFileProperty azureFileProperty) {
            schemaMapper = new CsvMapper();
            SimpleModule hfpModule = new SimpleModule();
            hfpModule.addDeserializer(Time.class, new HfpTimeDeserializer());
            schemaMapper.registerModule(hfpModule);
            findHfpType(azureFileProperty);
            blobContainerClient.getBlobClient(azureFileProperty.getFilePath())
                    .download(this);
        }

        private void findHfpType(AzureFileProperty azureFileProperty) {
            if (azureFileProperty.getEventType().equals(AzureFileProperty.BlobStorageFilenameStrategy.HFPFilenamePrefix.VEHICLEPOSITION)) {
                hfpType = VehiclePosition.class;
            }
            if (azureFileProperty.getEventType().equals(AzureFileProperty.BlobStorageFilenameStrategy.HFPFilenamePrefix.OTHEREVENT)) {
                hfpType = OtherEvent.class;
            }

            if (azureFileProperty.getEventType().equals(AzureFileProperty.BlobStorageFilenameStrategy.HFPFilenamePrefix.LIGHTPRIORITYEVENT)) {
                hfpType = LightPriorityEvent.class;

            }

            if (azureFileProperty.getEventType().equals(AzureFileProperty.BlobStorageFilenameStrategy.HFPFilenamePrefix.STOPEVENT)) {
                hfpType = StopEvent.class;
            }

            if (hfpType == null) {
                throw new IllegalArgumentException("Unknown HFP Type");
            }
        }

        @Override
        public synchronized void write(int i) throws IOException {
            final char writable = (char) i;
            builder.append(writable);
            if (!String.valueOf(writable).matches(".")) {
                builder.append(writable);
                final CsvSchema hfpSchema = schemaMapper.schemaFor(hfpType).withoutHeader();
                final Event readEvent = schemaMapper.readerFor(hfpType).with(hfpSchema).readValue(builder.toString());
                this.events.add(readEvent);
                builder = new StringBuilder();
            }
        }

        public Event readEvent() {
            return events.remove(events.size() - 1);
        }

        private class HfpTimeDeserializer extends JsonDeserializer<Time> {
            private final SimpleDateFormat simpledateformat;

            HfpTimeDeserializer() {
                this.simpledateformat = new SimpleDateFormat("HH:mm:ss");
            }

            @Override
            public Time deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                final String s = jsonParser.readValueAs(String.class);
                if (s.isEmpty()) {
                    return null;
                }
                try {
                    final Date parse = simpledateformat.parse(s);
                    return new Time(parse.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
    }
}

