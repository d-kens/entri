package com.entri.analytics.controller.api;

import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Organizer analytics data")
public interface AnalyticsApi {

    @Operation(
            operationId = "getOrganizerSummaryMetrics",
            summary = "Get Organizer Summary Metrics",
            description = "Returns top-level KPI cards for the authenticated organizer: revenue, wallet balance, tickets sold, and event counts."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary metrics retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('ORGANIZER')")
    @GetMapping("/summary")
    OrganizerSummaryMetricsResponse getOrganizerSummaryMetrics(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

}
