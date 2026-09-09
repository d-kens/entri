package com.entri.modules.tickets.service;

import com.entri.events.entity.Event;
import com.entri.events.repository.EventRepository;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.tickets.dto.VerifyCodeRequest;
import com.entri.tickets.entity.EventCheckInCode;
import com.entri.tickets.repository.EventCheckInCodeRepository;
import com.entri.tickets.service.CheckInCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInCodeServiceTest {

    private static final String EVENT_EXTERNAL_ID = "evt-abc-123";

    @Mock EventCheckInCodeRepository checkInCodeRepository;
    @Mock EventRepository eventRepository;

    @InjectMocks CheckInCodeService checkInCodeService;

    private Event buildEvent(Instant endTime) {
        return Event.builder()
                .id(1L)
                .externalId(EVENT_EXTERNAL_ID)
                .title("Some Event")
                .endTime(endTime)
                .build();
    }

    @Test
    void generateCode_eventNotFound_throwsResourceNotFoundException() {
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInCodeService.generateCode(EVENT_EXTERNAL_ID, "organizer-key"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_EXTERNAL_ID);
    }

    @Test
    void generateCode_eventAlreadyEnded_throwsBadRequestException() {
        var event = buildEvent(Instant.now().minus(1, ChronoUnit.DAYS));
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> checkInCodeService.generateCode(EVENT_EXTERNAL_ID, "organizer-key"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already ended");
    }

    @Test
    void generateCode_success_returnsUppercaseEightCharacterCode() {
        var endTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var event = buildEvent(endTime);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        var response = checkInCodeService.generateCode(EVENT_EXTERNAL_ID, "organizer-key");

        assertThat(response.code()).hasSize(8).isUpperCase();
        assertThat(response.eventExternalId()).isEqualTo(EVENT_EXTERNAL_ID);
        assertThat(response.expiresAt()).isEqualTo(endTime);
    }

    @Test
    void generateCode_success_savesCodeWithEventExpiryAndCreator() {
        var endTime = Instant.now().plus(1, ChronoUnit.DAYS);
        var event = buildEvent(endTime);
        when(eventRepository.findByExternalId(EVENT_EXTERNAL_ID)).thenReturn(Optional.of(event));

        checkInCodeService.generateCode(EVENT_EXTERNAL_ID, "organizer-key");

        var captor = ArgumentCaptor.forClass(EventCheckInCode.class);
        verify(checkInCodeRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getEvent()).isEqualTo(event);
        assertThat(saved.getExpiresAt()).isEqualTo(endTime);
        assertThat(saved.getCreatedBy()).isEqualTo("organizer-key");
    }

    @Test
    void verifyCode_invalidOrExpiredCode_throwsBadRequestException() {
        when(checkInCodeRepository.findValidCode(anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInCodeService.verifyCode(new VerifyCodeRequest("bad-code")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired check-in code");
    }

    @Test
    void verifyCode_validCode_returnsEventDetails() {
        var event = buildEvent(Instant.now().plus(1, ChronoUnit.DAYS));
        var checkInCode = EventCheckInCode.builder()
                .code("ABCD1234")
                .event(event)
                .expiresAt(event.getEndTime())
                .createdBy("organizer-key")
                .build();
        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(checkInCode));

        var response = checkInCodeService.verifyCode(new VerifyCodeRequest("abcd1234"));

        assertThat(response.eventExternalId()).isEqualTo(EVENT_EXTERNAL_ID);
        assertThat(response.eventTitle()).isEqualTo("Some Event");
    }

    @Test
    void verifyCode_trimsAndUppercasesCodeBeforeLookup() {
        var event = buildEvent(Instant.now().plus(1, ChronoUnit.DAYS));
        var checkInCode = EventCheckInCode.builder()
                .code("ABCD1234")
                .event(event)
                .expiresAt(event.getEndTime())
                .createdBy("organizer-key")
                .build();
        when(checkInCodeRepository.findValidCode(any(), any())).thenReturn(Optional.of(checkInCode));

        checkInCodeService.verifyCode(new VerifyCodeRequest("  abcd1234  "));

        verify(checkInCodeRepository).findValidCode(eq("ABCD1234"), any());
    }
}
