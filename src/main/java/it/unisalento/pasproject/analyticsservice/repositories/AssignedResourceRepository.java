package it.unisalento.pasproject.analyticsservice.repositories;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssignedResourceRepository extends MongoRepository<AssignedResource, String>{
}
