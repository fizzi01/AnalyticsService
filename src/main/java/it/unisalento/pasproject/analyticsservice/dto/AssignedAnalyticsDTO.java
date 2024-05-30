package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedAnalyticsDTO {

    private String taskId;
    private String emailUtente;

    private boolean isComplete;

    private LocalDateTime assignedTime;
    private LocalDateTime completedTime;

    private LocalDateTime lastUpdate;
}
