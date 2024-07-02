package it.unisalento.pasproject.analyticsservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedAnalyticsDTO {

    private String id;
    private String taskId;
    private String emailUtente;

    private boolean isComplete;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime assignedTime;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdate;
}
