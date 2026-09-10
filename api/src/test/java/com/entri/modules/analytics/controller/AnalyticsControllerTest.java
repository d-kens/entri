package com.entri.modules.analytics.controller;

import com.entri.analytics.controller.AnalyticsController;
import com.entri.analytics.dto.OrganizerSummaryMetricsResponse;
import com.entri.analytics.service.AnalyticsService;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    AnalyticsService analyticsService;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AnalyticsController controller = new AnalyticsController(analyticsService);
        User user = User.builder().externalKey("organizer-key").role(Role.ORGANIZER).build();
        UserPrincipal requestingUser = new UserPrincipal(user);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new UserPrincipalArgumentResolver(requestingUser))
                .build();
    }

    @Test
    void getOrganizerSummaryMetrics_returnsMetricsForRequestingOrganizer() throws Exception {
        var response = new OrganizerSummaryMetricsResponse(BigDecimal.valueOf(1000), 50L, 3L, 1L);
        when(analyticsService.getOrganizerSummaryMetrics(eq("organizer-key"))).thenReturn(response);

        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1000))
                .andExpect(jsonPath("$.totalTicketsSold").value(50));
    }

    static class UserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        private final UserPrincipal principal;

        UserPrincipalArgumentResolver(UserPrincipal principal) {
            this.principal = principal;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return principal;
        }
    }
}
