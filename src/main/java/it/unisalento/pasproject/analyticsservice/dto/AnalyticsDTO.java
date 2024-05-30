package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AnalyticsDTO {

    //Contiene le statistiche complete della REC

    private double energyConsumed;
    private double computingPowerUsed;

    private int activeMemberCount;
    private int activeUserCount;

    private int tasksSubmitted;
    private int tasksCompleted;

    private double workHours;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
