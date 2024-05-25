package it.unisalento.pasproject.analyticsservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Document(collection = "assignment_analytics")
public class AssignmentAnalytics {

    private String taskId;

    private List<AssignedResource> assignedResources;

    private boolean isComplete;

    private LocalDateTime assignedTime;
    private LocalDateTime completedTime;

    private LocalDateTime lastUpdate;
}
