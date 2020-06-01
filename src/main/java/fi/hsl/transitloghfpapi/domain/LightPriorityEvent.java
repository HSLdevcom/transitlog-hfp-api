package fi.hsl.transitloghfpapi.domain;

import fi.hsl.common.hfp.proto.*;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
public class LightPriorityEvent extends Event {

    private Integer tlp_requestid;
    private String tlp_requesttype;
    private String tlp_prioritylevel;
    private String tlp_reason;
    private Integer tlp_att_seq;
    private String tlp_decision;
    private Integer sid;
    private Integer signal_groupid;
    private Integer tlp_signalgroupnbr;
    private Integer tlp_line_configid;
    private Integer tlp_point_configid;
    private Integer tlp_frequency;
    private String tlp_protocol;

    public LightPriorityEvent(Hfp.Topic topic, Hfp.Payload payload) {
        super(topic, payload);
        this.tlp_requestid = payload.hasTlpRequestid() ? payload.getTlpRequestid() : null;
        this.tlp_requesttype = payload.hasTlpRequesttype() ? payload.getTlpRequesttype().toString() : null;
        this.tlp_prioritylevel = payload.hasTlpPrioritylevel() ? payload.getTlpPrioritylevel().toString() : null;
        this.tlp_reason = payload.hasTlpReason() ? payload.getTlpReason().toString() : null;
        this.tlp_att_seq = payload.hasTlpAttSeq() ? payload.getTlpAttSeq() : null;
        this.tlp_decision = payload.hasTlpDecision() ? payload.getTlpDecision().toString() : null;
        this.sid = payload.hasSid() ? payload.getSid() : null;
        this.signal_groupid = payload.hasSignalGroupid() ? payload.getSignalGroupid() : null;
        this.tlp_signalgroupnbr = payload.hasTlpSignalgroupnbr() ? payload.getTlpSignalgroupnbr() : null;
        this.tlp_line_configid = payload.hasTlpLineConfigid() ? payload.getTlpLineConfigid() : null;
        this.tlp_point_configid = payload.hasTlpPointConfigid() ? payload.getTlpPointConfigid() : null;
        this.tlp_frequency = payload.hasTlpFrequency() ? payload.getTlpFrequency() : null;
        this.tlp_protocol = payload.hasTlpProtocol() ? payload.getTlpProtocol() : null;
    }

    public LightPriorityEvent() {
    }
}
