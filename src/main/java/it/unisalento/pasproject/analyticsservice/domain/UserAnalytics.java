package it.unisalento.pasproject.analyticsservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;


import java.time.LocalDateTime;

@TimeSeries(collection = "user_analytics", timeField = "indexDate")
public class UserAnalytics {

    @Id
    private String id;
    private String userId;
    private int tasksSubmitted;
    private double energySaved;
    private int tasksCompleted;
    private double timeSpentOnTasks;

    private LocalDateTime indexDate; //data di inizio del monitoraggio (Ogni settimana si crea altro documento con data di inizio della settimana)
    private LocalDateTime lastUpdate; //data di ultimo aggiornamento


}
