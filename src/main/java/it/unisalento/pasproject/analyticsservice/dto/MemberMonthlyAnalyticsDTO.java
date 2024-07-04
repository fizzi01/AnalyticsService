package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberMonthlyAnalyticsDTO {
    private String memberEmail;
    private int month;
    private int year;
    private double totalWorkMinutes;
    private double totalEnergySold;
    private double totalComputingPowerSold;
    private int tasksCompleted;
    private int tasksInProgress;
    private int tasksAssigned;
}
