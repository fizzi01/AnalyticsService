package it.unisalento.pasproject.analyticsservice.service;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.AnalyticsMessageDTO;
import it.unisalento.pasproject.analyticsservice.dto.AssignedAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.AssignedResourceAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalyticsDataHandler {

    private final AssignmentAnalyticsRepository assignmentAnalyticsRepository;
    private final AssignedResourceRepository assignedResourceRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsDataHandler.class);


    @Autowired
    public AnalyticsDataHandler(AssignmentAnalyticsRepository assignmentAnalyticsRepository, AssignedResourceRepository assignedResourceRepository) {
        this.assignmentAnalyticsRepository = assignmentAnalyticsRepository;
        this.assignedResourceRepository = assignedResourceRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.analytics.name}")
    public void receiveUpdatedAssignmentData(AnalyticsMessageDTO message) {
        LOGGER.info("Received message: {}", message);
        if (message != null){
            if(message.getAssignedResource()!= null){
                AssignedResource assignedResource = getFromDto(message.getAssignedResource());
                assignedResourceRepository.save(assignedResource);
            }

            if(message.getAssignment() != null){
                AssignmentAnalytics assignmentAnalytics = getFromDto(message.getAssignment());
                assignmentAnalyticsRepository.save(assignmentAnalytics);
            }
        }
    }

    public Optional<AssignmentAnalytics> getExistingAssignmentAnalytics(String taskId){
        return assignmentAnalyticsRepository.findByTaskId(taskId);
    }

    public AssignedResource getFromDto(AssignedResourceAnalyticsDTO assignedResourceAnalyticsDTO){
        AssignedResource assignedResource = new AssignedResource();

        if(assignedResourceAnalyticsDTO.getId() != null)
            assignedResource.setId(assignedResourceAnalyticsDTO.getId());

        assignedResource.setTaskId(assignedResourceAnalyticsDTO.getTaskId());
        assignedResource.setHardwareId(assignedResourceAnalyticsDTO.getHardwareId());
        assignedResource.setHardwareName(assignedResourceAnalyticsDTO.getHardwareName());
        assignedResource.setMemberEmail(assignedResourceAnalyticsDTO.getMemberEmail());
        assignedResource.setAssignedSingleScore(assignedResourceAnalyticsDTO.getAssignedSingleScore());
        assignedResource.setAssignedMultiScore(assignedResourceAnalyticsDTO.getAssignedMultiScore());
        assignedResource.setAssignedOpenclScore(assignedResourceAnalyticsDTO.getAssignedOpenclScore());
        assignedResource.setAssignedVulkanScore(assignedResourceAnalyticsDTO.getAssignedVulkanScore());
        assignedResource.setAssignedCudaScore(assignedResourceAnalyticsDTO.getAssignedCudaScore());
        assignedResource.setAssignedEnergyConsumptionPerHour(assignedResourceAnalyticsDTO.getAssignedEnergyConsumptionPerHour());

        assignedResource.setAssignedTime(assignedResourceAnalyticsDTO.getAssignedTime());
        assignedResource.setCompletedTime(assignedResourceAnalyticsDTO.getCompletedTime());
        assignedResource.setLastUpdate(assignedResourceAnalyticsDTO.getLastUpdate());
        assignedResource.setHasCompleted(assignedResourceAnalyticsDTO.isHasCompleted());


        return assignedResource;
    }

    public AssignmentAnalytics getFromDto(AssignedAnalyticsDTO assignedAnalyticsDTO){
        AssignmentAnalytics assignmentAnalytics = new AssignmentAnalytics();

        if(assignedAnalyticsDTO.getId() != null)
            assignmentAnalytics.setId(assignedAnalyticsDTO.getId());

        assignmentAnalytics.setTaskId(assignedAnalyticsDTO.getTaskId());
        assignmentAnalytics.setEmailUtente(assignedAnalyticsDTO.getEmailUtente());
        assignmentAnalytics.setComplete(assignedAnalyticsDTO.isComplete());
        assignmentAnalytics.setAssignedTime(assignedAnalyticsDTO.getAssignedTime());
        assignmentAnalytics.setCompletedTime(assignedAnalyticsDTO.getCompletedTime());
        assignmentAnalytics.setLastUpdate(assignedAnalyticsDTO.getLastUpdate());

        return assignmentAnalytics;
    }

}
