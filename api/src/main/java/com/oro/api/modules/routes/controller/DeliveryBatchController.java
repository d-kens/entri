package com.oro.api.modules.routes.controller;

import com.oro.api.modules.routes.dto.AssignDriverDto;
import com.oro.api.modules.routes.dto.BatchResponseDto;
import com.oro.api.modules.routes.entity.BatchStatus;
import com.oro.api.modules.routes.service.DeliveryBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class DeliveryBatchController {

    private final DeliveryBatchService batchService;

    @GetMapping
    public ResponseEntity<List<BatchResponseDto>> getBatches(
            @RequestParam(required = false) BatchStatus status) {
        return ResponseEntity.ok(
                batchService.getBatchesByStatus(status).stream()
                        .map(batchService::toDto)
                        .toList()
        );
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<BatchResponseDto> getBatch(@PathVariable String externalId) {
        return ResponseEntity.ok(batchService.toDto(batchService.getBatchByExternalId(externalId)));
    }

    @PostMapping("/{externalId}/dispatch")
    public ResponseEntity<BatchResponseDto> dispatchBatch(@PathVariable String externalId) {
        return ResponseEntity.ok(batchService.toDto(batchService.dispatchBatch(externalId)));
    }

    @PatchMapping("/{externalId}/driver")
    public ResponseEntity<BatchResponseDto> assignDriver(
            @PathVariable String externalId,
            @Valid @RequestBody AssignDriverDto dto) {
        return ResponseEntity.ok(batchService.toDto(batchService.assignDriver(externalId, dto)));
    }
}
