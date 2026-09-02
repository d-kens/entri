package com.entri.analytics.controller.api;

import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.analytics.dto.PlatformSummaryMetricsResponse;
import com.entri.analytics.dto.SalesTrendResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(
            operationId = "getOrganizerSalesTrend",
            summary = "Get Organizer Sales Trend",
            description = "Returns time-series ticket sales and revenue data for the authenticated organizer. Use `period=7d` or `period=30d` for daily data points, `period=12m` for monthly."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sales trend retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid period value. Allowed: 7d, 30d, 12m",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('ORGANIZER')")
    @GetMapping("/sales-trend")
    SalesTrendResponse getOrganizerSalesTrend(
            @Parameter(description = "Time period. Allowed values: 7d, 30d, 12m", example = "30d")
            @RequestParam(defaultValue = "30d") String period,

            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal requestingUser
    );

    @Operation(
            operationId = "getPlatformSummaryMetrics",
            summary = "Get Platform Summary Metrics",
            description = "Returns platform-wide KPI cards for the admin: total organizers, events by status, tickets sold, and gross revenue."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform summary metrics retrieved successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/platform/summary")
    PlatformSummaryMetricsResponse getPlatformSummaryMetrics();

    @Operation(
            operationId = "getPlatformSalesTrend",
            summary = "Get Platform Sales Trend",
            description = "Returns platform-wide time-series ticket sales and revenue data. Use `period=7d` or `period=30d` for daily, `period=12m` for monthly."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform sales trend retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid period value. Allowed: 7d, 30d, 12m",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin access required",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/platform/sales-trend")
    SalesTrendResponse getPlatformSalesTrend(
            @Parameter(description = "Time period. Allowed values: 7d, 30d, 12m", example = "30d")
            @RequestParam(defaultValue = "30d") String period
    );
}
