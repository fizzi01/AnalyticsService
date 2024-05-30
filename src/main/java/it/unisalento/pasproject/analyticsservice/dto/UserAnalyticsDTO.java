package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class UserAnalyticsDTO {

    private String userEmail;

    private int tasksSubmitted;

    private int taskOngoing;
    private int tasksCompleted;

    private double energySaved;
    private double computingPowerUsed;

    private double timeSpentOnTasks;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
