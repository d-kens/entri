package com.entri.tickets.service;

import com.entri.events.repository.EventRepository;
import com.entri.exception.BadRequestException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.tickets.dto.CheckInCodeResponse;
import com.entri.tickets.dto.VerifyCodeRequest;
import com.entri.tickets.dto.VerifyCodeResponse;
import com.entri.tickets.entity.EventCheckInCode;
import com.entri.tickets.repository.EventCheckInCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckInCodeService {

    private final EventCheckInCodeRepository checkInCodeRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CheckInCodeResponse generateCode(String eventExternalId, String organizerExternalKey) {
        var event = eventRepository.findByExternalId(eventExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventExternalId));

        if (event.getEndTime().isBefore(Instant.now())) {
            throw new BadRequestException("Cannot generate a check-in code for an event that has already ended");
        }

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        checkInCodeRepository.save(EventCheckInCode.builder()
                .code(code)
                .event(event)
                .expiresAt(event.getEndTime())
                .createdBy(organizerExternalKey)
                .build());

        return new CheckInCodeResponse(code, eventExternalId, event.getEndTime());
    }

    @Transactional(readOnly = true)
    public VerifyCodeResponse verifyCode(VerifyCodeRequest request) {
        var checkInCode = checkInCodeRepository.findValidCode(request.code().trim().toUpperCase(), Instant.now())
                .orElseThrow(() -> new BadRequestException("Invalid or expired check-in code"));

        var event = checkInCode.getEvent();
        return new VerifyCodeResponse(event.getExternalId(), event.getTitle());
    }
}
