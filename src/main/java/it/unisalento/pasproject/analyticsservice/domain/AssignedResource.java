package it.unisalento.pasproject.analyticsservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedResource {

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
    private boolean hasCompleted;
}
