package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsMessageDTO {
    private AssignedResourceAnalyticsDTO assignedResource;
    private AssignedAnalyticsDTO assignment;
}
