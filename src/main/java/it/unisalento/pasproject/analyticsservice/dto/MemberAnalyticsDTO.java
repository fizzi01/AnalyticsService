package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberAnalyticsDTO {

    private String memberEmail;
    private double workMinutes;

    private double energyConsumed;
    private double computingPower;

    private int tasksCompleted;
    private int tasksInProgress;
    private int tasksAssigned;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
