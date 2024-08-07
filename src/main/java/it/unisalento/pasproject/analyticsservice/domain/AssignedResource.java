package it.unisalento.pasproject.analyticsservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "assigned_resource_analytics")
public class AssignedResource {

    @Id
    private String id;

    private String taskId;

    private String hardwareId;
    private String hardwareName;

    private String memberEmail;

    private double assignedSingleScore;
    private double assignedMultiScore;
    private double assignedOpenclScore;
    private double assignedVulkanScore;
    private double assignedCudaScore;

    private double assignedEnergyConsumptionPerHour;

    private LocalDateTime assignedTime;
    private LocalDateTime completedTime;

    private LocalDateTime lastUpdate;
    private boolean hasCompleted;
}
