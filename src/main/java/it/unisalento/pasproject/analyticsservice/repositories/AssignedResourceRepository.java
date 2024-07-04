package it.unisalento.pasproject.analyticsservice.repositories;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignedResourceRepository extends MongoRepository<AssignedResource, String>{
    List<AssignedResource> findByMemberEmailAndAssignedTimeGreaterThanEqualAndCompletedTimeLessThanEqual(String memberEmail, LocalDateTime startDate, LocalDateTime endDate);
}
