package fi.hsl.transitloghfpapi.domain.repositories;

import fi.hsl.transitloghfpapi.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface BlobRepository extends JpaRepository<Blob, Long> {
    public Blob findByJobExecutionId(Long jobExecutionId);
}
