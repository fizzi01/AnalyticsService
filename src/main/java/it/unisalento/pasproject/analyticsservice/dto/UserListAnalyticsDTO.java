package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserListAnalyticsDTO {
    private String userEmail;

    private int tasksSubmitted;

    private int tasksOngoing;
    private int tasksCompleted;

    private double energySaved;
    private double computingPowerUsed;

    private double timeSpentOnTasks;

    private int day;
    private int month;
    private int year;
}
