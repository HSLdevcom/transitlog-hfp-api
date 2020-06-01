package fi.hsl.transitloghfpapi.domain;

import com.fasterxml.jackson.annotation.*;
import fi.hsl.common.hfp.*;
import fi.hsl.common.hfp.proto.*;
import lombok.*;
import lombok.extern.slf4j.*;

import javax.persistence.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.Date;
import java.util.*;
import java.util.function.*;

@MappedSuperclass
@EqualsAndHashCode
@JsonSubTypes({
        @JsonSubTypes.Type(value = VehiclePosition.class, name = "vehicleposition"),
        @JsonSubTypes.Type(value = LightPriorityEvent.class, name = "lightpriorityevent"),
        @JsonSubTypes.Type(value = OtherEvent.class, name = "otherevent"),
        @JsonSubTypes.Type(value = StopEvent.class, name = "stopevent"),
        @JsonSubTypes.Type(value = UnsignedEvent.class, name = "unsignedevent")
})
@Data
@Slf4j
public abstract class Event {
    private Timestamp tst;
    private String unique_vehicle_id;
    private String event_type;
    private String journey_type;
    @Id
    private UUID uuid;
    @Version
    private Long version;
    private Timestamp received_at;
    private String topic_prefix;
    private String topic_version;
    private Boolean is_ongoing;
    private String mode;
    private Integer owner_operator_id;
    private Integer vehicle_number;
    private String route_id;
    private Integer direction_id;
    private String headsign;
    private Time journey_start_time;
    private String next_stop_id;
    private Integer geohash_level;
    private Double topic_latitude;
    private Double topic_longitude;
    private Double lat;
    @Column(name = "long")
    private Double longitude;
    private String desi;
    private Integer dir;
    private Integer oper;
    private Integer veh;
    private BigInteger tsi;
    private Double spd;
    private Integer hdg;
    private Double acc;
    private Integer dl;
    private Double odo;
    private Boolean drst;
    private Date oday;
    private Integer jrn;
    private Integer line;
    private Time start;
    @Column(name = "loc")
    private String location_quality_method;
    private Integer stop;
    private String route;
    private Integer occu;
    private Integer seq;
    private Integer dr_type;

    public Event(Hfp.Topic topic, Hfp.Payload payload) {
        this.uuid = UUID.randomUUID();
        this.tst = payload.hasTst() ? safeParse(payload.getTst()).get() : null;
        this.journey_type = topic.hasJourneyType() ? topic.getJourneyType().toString() : null;
        this.event_type = topic.hasEventType() ? topic.getEventType().toString() : null;
        this.unique_vehicle_id = topic.hasUniqueVehicleId() ? topic.getUniqueVehicleId() : null;
        this.received_at = topic.hasReceivedAt() ? Timestamp.from(Instant.ofEpochMilli(topic.getReceivedAt())) : null;
        this.topic_prefix = topic.hasTopicPrefix() ? topic.getTopicPrefix() : null;
        this.topic_version = topic.hasTopicVersion() ? topic.getTopicVersion() : null;
        this.is_ongoing = topic.hasTemporalType() ? topic.getTemporalType() == Hfp.Topic.TemporalType.ongoing : null;
        this.mode = topic.hasTransportMode() ? topic.getTransportMode().toString() : null;
        this.owner_operator_id = topic.hasOperatorId() ? topic.getOperatorId() : null;
        this.vehicle_number = topic.hasVehicleNumber() ? topic.getVehicleNumber() : null;
        this.route_id = topic.hasRouteId() ? topic.getRouteId() : null;
        this.direction_id = topic.hasDirectionId() ? topic.getDirectionId() : null;
        this.headsign = topic.hasHeadsign() ? topic.getHeadsign() : null;
        Optional<Time> maybeStartTime = wrapToOptional(topic::hasStartTime, topic::getStartTime).flatMap(HfpParser::safeParseTime);
        this.journey_start_time = maybeStartTime.orElse(null);
        this.next_stop_id = topic.hasNextStop() ? topic.getNextStop() : null;
        this.geohash_level = topic.hasGeohashLevel() ? topic.getGeohashLevel() : null;
        this.topic_latitude = topic.hasLatitude() ? topic.getLatitude() : null;
        this.topic_longitude = topic.hasLongitude() ? topic.getLongitude() : null;
        this.lat = payload.hasLat() ? payload.getLat() : null;
        this.longitude = payload.hasLong() ? payload.getLong() : null;
        this.desi = payload.hasDesi() ? payload.getDesi() : null;
        Optional<Integer> maybeDirection = wrapToOptional(payload::hasDir, payload::getDir).flatMap(HfpParser::safeParseInt);
        this.dir = maybeDirection.orElse(null);
        this.oper = payload.hasOper() ? payload.getOper() : null;
        this.veh = payload.hasVeh() ? payload.getVeh() : null;
        this.tsi = payload.hasTsi() ? BigInteger.valueOf(payload.getTsi()) : null;
        this.spd = payload.hasSpd() ? payload.getSpd() : null;
        this.hdg = payload.hasHdg() ? payload.getHdg() : null;
        this.acc = payload.hasAcc() ? payload.getAcc() : null;
        this.dl = payload.hasDl() ? payload.getDl() : null;
        this.odo = payload.hasOdo() ? payload.getOdo() : null;
        Optional<Boolean> maybeDoors = wrapToOptional(payload::hasDrst, payload::getDrst).flatMap(HfpParser::safeParseBoolean);
        this.drst = maybeDoors.orElse(null);
        Optional<java.sql.Date> maybeOperatingDay = wrapToOptional(payload::hasOday, payload::getOday).flatMap(HfpParser::safeParseDate);
        this.oday = maybeOperatingDay.orElse(null);
        this.jrn = payload.hasJrn() ? payload.getJrn() : null;
        this.line = payload.hasLine() ? payload.getLine() : null;
        Optional<Time> maybeStart = wrapToOptional(payload::hasStart, payload::getStart).flatMap(HfpParser::safeParseTime);
        this.start = maybeStart.orElse(null);
        this.location_quality_method = payload.hasLoc() ? payload.getLoc().toString() : null;
        this.stop = payload.hasStop() ? payload.getStop() : null;
        this.route = payload.hasRoute() ? payload.getRoute() : null;
        this.occu = payload.hasOccu() ? payload.getOccu() : null;
        this.seq = payload.hasSeq() ? payload.getSeq() : null;
        this.dr_type = payload.hasDrType() ? payload.getDrType() : null;
    }

    private Optional<Timestamp> safeParse(String time) {
        if (time == null) {
            return Optional.empty();
        } else {
            try {
                OffsetDateTime offsetDt = OffsetDateTime.parse(time);
                return Optional.of(Timestamp.valueOf(offsetDt.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()));
            } catch (Exception var2) {
                log.error("Failed to convert {} to java.sql.Timestamp", time);
                return Optional.empty();
            }

        }
    }

    static <T> Optional<T> wrapToOptional(Supplier<Boolean> isPresent, Supplier<T> getter) {
        if (isPresent.get()) {
            return Optional.of(getter.get());
        }
        return Optional.empty();
    }

    public Event() {
    }
}
