package it.unisalento.pasproject.analyticsservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.TimeSeries;

import java.time.LocalDateTime;

@TimeSeries(collection = "member_analytics", timeField = "indexDate")
public class MemberAnalytics {

    @Id
    private String id;
    private String memberId;
    private double workHours;
    private double energyConsumed;
    private double computingPower;
    private int tasksCompleted;
    private int tasksInProgress;
    private int tasksAssigned;

    private LocalDateTime indexDate; //data di inizio del monitoraggio (Ogni settimana si crea altro documento con data di inizio della settimana)
    private LocalDateTime lastUpdate; //data di ultimo aggiornamento

}
