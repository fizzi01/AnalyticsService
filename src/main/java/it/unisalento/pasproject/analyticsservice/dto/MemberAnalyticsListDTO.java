package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberAnalyticsListDTO {
    private List<MemberAnalyticsDTO> memberAnalyticsDTOList;

    public MemberAnalyticsListDTO(List<MemberAnalyticsDTO> memberAnalyticsDTOList) {
        this.memberAnalyticsDTOList = memberAnalyticsDTOList;
    }
}
