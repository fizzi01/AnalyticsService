package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TaskAnalyticsDTO {
    private String taskId;

    private double energySaved;
    private double computingPowerUsed;

}
