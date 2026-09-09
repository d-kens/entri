package com.entri.modules.events.controller;

import com.entri.events.controller.TicketTypeController;
import com.entri.events.dto.TicketTypeRequest;
import com.entri.events.dto.TicketTypeResponse;
import com.entri.events.entity.TicketTypeAvailabilityStatus;
import com.entri.events.entity.TicketTypeSaleStatus;
import com.entri.events.service.TicketTypeService;
import com.entri.exception.ForbiddenException;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.ResourceNotFoundException;
import com.entri.security.UserPrincipal;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TicketTypeControllerTest {

    @Mock
    TicketTypeService ticketTypeService;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();
    UserPrincipal requestingUser;

    @BeforeEach
    void setUp() {
        TicketTypeController controller = new TicketTypeController(ticketTypeService);
        User user = User.builder().externalKey("organizer-key").role(Role.ORGANIZER).build();
        requestingUser = new UserPrincipal(user);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new UserPrincipalArgumentResolver(requestingUser))
                .build();
    }

    private TicketTypeResponse sampleResponse() {
        return new TicketTypeResponse(1L, "VIP", "VIP access", BigDecimal.TEN, 100, 80, 4,
                null, null, TicketTypeSaleStatus.ON_SALE, TicketTypeAvailabilityStatus.AVAILABLE);
    }

    @Test
    void getTicketType_existingId_returnsTicketType() throws Exception {
        when(ticketTypeService.getTicketType(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/ticket-types/{ticketTypeId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("VIP"));
    }

    @Test
    void getTicketType_notFound_returns404() throws Exception {
        when(ticketTypeService.getTicketType(99L)).thenThrow(new ResourceNotFoundException("Ticket type not found"));

        mockMvc.perform(get("/ticket-types/{ticketTypeId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTicketType_validRequest_returnsUpdatedTicketType() throws Exception {
        var request = new TicketTypeRequest("VIP", "VIP access", BigDecimal.TEN, 100, 4, null, null);
        when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeRequest.class), any(UserPrincipal.class)))
                .thenReturn(sampleResponse());

        mockMvc.perform(put("/ticket-types/{ticketTypeId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("VIP"));
    }

    @Test
    void updateTicketType_negativePrice_returns400() throws Exception {
        var request = new TicketTypeRequest("VIP", "VIP access", BigDecimal.valueOf(-1), 100, 4, null, null);

        mockMvc.perform(put("/ticket-types/{ticketTypeId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTicketType_notOwner_returns403() throws Exception {
        var request = new TicketTypeRequest("VIP", "VIP access", BigDecimal.TEN, 100, 4, null, null);
        when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeRequest.class), any(UserPrincipal.class)))
                .thenThrow(new ForbiddenException("You are not authorized to perform this action"));

        mockMvc.perform(put("/ticket-types/{ticketTypeId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTicketType_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/ticket-types/{ticketTypeId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTicketType_notOwner_returns403() throws Exception {
        org.mockito.Mockito.doThrow(new ForbiddenException("You are not authorized to perform this action"))
                .when(ticketTypeService).deleteTicketType(eq(1L), any(UserPrincipal.class));

        mockMvc.perform(delete("/ticket-types/{ticketTypeId}", 1L))
                .andExpect(status().isForbidden());
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
