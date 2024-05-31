package it.unisalento.pasproject.analyticsservice.controller;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class TestController {

    @Autowired
    private AssignedResourceRepository assignedResourceRepository;

    @Autowired
    private AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    @GetMapping("/test/res")
    public List<AssignedResource> getAssignedResources() {
        return assignedResourceRepository.findAll();
    }

    @GetMapping("/test/ass")
    public List<AssignmentAnalytics> getAssignments(){
        return assignmentAnalyticsRepository.findAll();
    }
}
