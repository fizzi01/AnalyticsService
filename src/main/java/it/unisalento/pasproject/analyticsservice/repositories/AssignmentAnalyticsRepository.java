package it.unisalento.pasproject.analyticsservice.repositories;

import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AssignmentAnalyticsRepository extends MongoRepository<AssignmentAnalytics, String>{
    Optional<AssignmentAnalytics> findByTaskId(String taskId);

    double getWorkHours(String memberEmail);
}
