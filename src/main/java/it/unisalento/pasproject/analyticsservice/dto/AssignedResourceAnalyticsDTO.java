package it.unisalento.pasproject.analyticsservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedResourceAnalyticsDTO {

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

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime assignedTime;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdate;
    private boolean hasCompleted;
}
