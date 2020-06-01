package fi.hsl.transitloghfpapi.domain;

import fi.hsl.common.hfp.proto.*;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
public class VehiclePosition extends Event {
    public VehiclePosition(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload);
    }

    public VehiclePosition() {
    }
}
