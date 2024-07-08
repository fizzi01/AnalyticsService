package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminListAnalyticsDTO {
    private double energyConsumed;
    private double computingPowerUsed;

    private int activeMemberCount;
    private int activeUserCount;

    private int tasksSubmitted;
    private int tasksCompleted;

    private double workMinutes;

    private int year;
    private int month;
    private int day;
}
