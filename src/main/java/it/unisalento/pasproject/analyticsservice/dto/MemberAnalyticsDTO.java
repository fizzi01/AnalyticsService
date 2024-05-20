package it.unisalento.pasproject.analyticsservice.dto;

import java.time.LocalDateTime;

public class MemberAnalyticsDTO {

    private String id;
    private String memberId;
    private double workHours;
    private double energyConsumed;
    private double computingPower;
    private int tasksCompleted;
    private int tasksInProgress;
    private int tasksAssigned;

    private LocalDateTime indexDate;
    private LocalDateTime lastUpdate;
}
