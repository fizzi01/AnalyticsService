package it.unisalento.pasproject.analyticsservice.dto;

import java.time.LocalDateTime;

public class UserAnalyticsDTO {
    private String id;
    private String userId;
    private int tasksSubmitted;
    private double energySaved;
    private int tasksCompleted;
    private double timeSpentOnTasks;

    private LocalDateTime indexDate; //data di inizio del monitoraggio (Ogni settimana si crea altro documento con data di inizio della settimana)
    private LocalDateTime lastUpdate;
}
