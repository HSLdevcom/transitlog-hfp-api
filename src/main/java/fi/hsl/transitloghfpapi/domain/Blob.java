package fi.hsl.transitloghfpapi.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
public class Blob {
    @Id
    private Long id;

    private Long jobExecutionId;
    private String downloadLink;
}
