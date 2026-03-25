package com.parrcel.api.modules.wallet.controller;

import com.parrcel.api.common.dto.PageResponse;
import com.parrcel.api.modules.wallet.dto.WalletResponse;
import com.parrcel.api.modules.wallet.dto.WalletTransactionResponse;
import com.parrcel.api.modules.wallet.dto.WithdrawRequest;
import com.parrcel.api.modules.wallet.dto.WithdrawResponse;
import com.parrcel.api.modules.wallet.entity.Wallet;
import com.parrcel.api.modules.wallet.entity.WalletTransaction;
import com.parrcel.api.modules.wallet.mapper.WalletMapper;
import com.parrcel.api.modules.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;
    private final WalletMapper walletMapper;

    @GetMapping
    public ResponseEntity<WalletResponse> getMyWallet(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("Fetching wallet for user: {}", userId);

        Wallet wallet = walletService.getOrCreateWallet(userId);
        WalletResponse response = walletMapper.toResponse(wallet);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public WithdrawResponse withDraw(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return walletService.withDraw(request, userId);
    }

    @GetMapping("/transactions")
    public ResponseEntity<PageResponse<WalletTransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Fetching wallet transactions for user: {} - page: {}, size: {}",
                userId, page, size);

        Wallet wallet = walletService.getOrCreateWallet(userId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<WalletTransaction> transactionsPage = walletService.getWalletTransactions(
                wallet.getId(),
                pageable
        );

        PageResponse<WalletTransactionResponse> response = PageResponse.<WalletTransactionResponse>builder()
                .content(transactionsPage.getContent().stream()
                        .map(walletMapper::toWalletTransactionresponse)
                        .toList())
                .pageNumber(transactionsPage.getNumber())
                .pageSize(transactionsPage.getSize())
                .totalElements(transactionsPage.getTotalElements())
                .totalPages(transactionsPage.getTotalPages())
                .last(transactionsPage.isLast())
                .first(transactionsPage.isFirst())
                .build();

        return ResponseEntity.ok(response);
    }
}
