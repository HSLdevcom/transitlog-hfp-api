package fi.hsl.transitloghfpapi.domain.repositories;

import fi.hsl.transitloghfpapi.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
}
