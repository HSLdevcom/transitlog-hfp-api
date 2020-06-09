package fi.hsl.transitloghfpapi.servehfp.azure;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.*;
import fi.hsl.transitloghfpapi.domain.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.*;

public class AzureEventConsumer extends OutputStream {
    private final CsvMapper schemaMapper;
    private final Class<? extends Event> hfpType;
    StringBuilder builder = new StringBuilder();
    List<Event> events = new CopyOnWriteArrayList<>();

    AzureEventConsumer(Class<? extends Event> hfpType) {
        checkNotNull(hfpType);
        schemaMapper = new CsvMapper();
        schemaMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.hfpType = hfpType;

    }

    @Override
    public synchronized void write(int i) throws IOException {
        final char writable = (char) i;
        builder.append(writable);
        if (!String.valueOf(writable).matches(".")) {
            builder.append(writable);
            final CsvSchema hfpSchema = schemaMapper.schemaFor(hfpType).withoutHeader();
            final Event o = schemaMapper.readerFor(hfpType).with(hfpSchema).readValue(builder.toString());
            this.events.add(o);
            builder = new StringBuilder();
        }
    }

    public Event readEvent() {
        return null;
    }
}
