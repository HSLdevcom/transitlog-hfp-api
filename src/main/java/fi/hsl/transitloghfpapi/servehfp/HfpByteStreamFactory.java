package fi.hsl.transitloghfpapi.servehfp;

import fi.hsl.transitloghfpapi.domain.*;
import org.javatuples.*;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

class HfpByteStreamFactory {
    static final String CSV = "csv/";

    static List<Pair<String, ByteToEventStream>> createHfpStream(LocalDateTime startDate, LocalDateTime endDate, List<Class<? extends Event>> eventTypes) {
        //Split to hourly ranges
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
        List<LocalDateTime> localDates = new ArrayList<>();

        while (startDate.isBefore(endDate)) {
            localDates.add(startDate);
            startDate = startDate.plus(1, ChronoUnit.HOURS);
        }
        return localDates.stream()
                .flatMap(localDateTime -> createHfpPairs(localDateTime, dateTimeFormatter, eventTypes).stream())
                .collect(Collectors.toList());
    }

    private static List<Pair<String, ByteToEventStream>> createHfpPairs(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter, List<Class<? extends Event>> eventTypes) {
        return eventTypes.stream()
                .map(eventType -> createHfpPair(localDateTime, dateTimeFormatter, eventType))
                .collect(Collectors.toList());
    }

    private static Pair<String, ByteToEventStream> createHfpPair(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter, Class<? extends Event> eventType) {
        final String properDateFormat = localDateTime.format(dateTimeFormatter);


        String hfpFileName = CSV + eventType.getSimpleName() + "/" + properDateFormat + ".csv";
        final ByteToEventStream byteToEventStream = new ByteToEventStream(eventType);
        return new Pair<>(hfpFileName, byteToEventStream);
    }

}
