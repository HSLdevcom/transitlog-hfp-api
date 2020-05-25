package fi.hsl.transitloghfpapi.domain;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

@Repository
public interface HfpRepository extends JpaRepository<Event, Long> {


}
