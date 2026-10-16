package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.ManageRefundsUseCase;
import com.nexusmarket.infrastructure.web.dto.CreateRefundRequest;
import com.nexusmarket.infrastructure.web.dto.RefundResponse;
import com.nexusmarket.infrastructure.web.mapper.RefundWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the {@link ManageRefundsUseCase} input port.
 *
 * <p>Fase 2.1 of docs/PLAN_API_WEB.md.</p>
 */
@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final ManageRefundsUseCase manageRefundsUseCase;

    public RefundController(ManageRefundsUseCase manageRefundsUseCase) {
        this.manageRefundsUseCase = manageRefundsUseCase;
    }

    @PostMapping
    public RefundResponse request(ActorContext actor, @Valid @RequestBody CreateRefundRequest request) {
        return RefundWebMapper.toResponse(
                manageRefundsUseCase.requestRefund(actor, RefundWebMapper.toCommand(request)));
    }
}
