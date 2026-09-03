package com.entri.analytics.controller;

import com.entri.analytics.controller.api.AnalyticsApi;
import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.analytics.service.AnalyticsService;
import com.entri.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController implements AnalyticsApi {

    private final AnalyticsService analyticsService;

    @Override
    public OrganizerSummaryMetricsResponse getOrganizerSummaryMetrics(UserPrincipal requestingUser) {
        return analyticsService.getOrganizerSummaryMetrics(requestingUser.getExternalKey());
    }

}
