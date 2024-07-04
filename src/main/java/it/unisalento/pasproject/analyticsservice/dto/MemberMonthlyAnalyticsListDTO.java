package it.unisalento.pasproject.analyticsservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MemberMonthlyAnalyticsListDTO {
    private List<MemberMonthlyAnalyticsDTO> memberMonthlyAnalyticsList;

    public MemberMonthlyAnalyticsListDTO() {
        this.memberMonthlyAnalyticsList = new ArrayList<>();
    }
}
