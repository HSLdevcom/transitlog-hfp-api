package fi.hsl.transitloghfpapi.servehfp.azure;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.*;
import fi.hsl.transitloghfpapi.domain.*;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.*;

public class AzureEventConsumer extends OutputStream {
    private final CsvMapper schemaMapper;
    private final Class<? extends Event> hfpType;
    private StringBuilder builder = new StringBuilder();
    private List<Event> events = new CopyOnWriteArrayList<>();

    AzureEventConsumer(Class<? extends Event> hfpType) {
        checkNotNull(hfpType);
        schemaMapper = new CsvMapper();
        SimpleModule hfpModule = new SimpleModule();
        hfpModule.addDeserializer(Time.class, new HfpTimeDeserializer());
        schemaMapper.registerModule(hfpModule);
        this.hfpType = hfpType;

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

    private static class HfpTimeDeserializer extends JsonDeserializer<Time> {
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
