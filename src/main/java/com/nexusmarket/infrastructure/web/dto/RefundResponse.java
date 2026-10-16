package com.nexusmarket.infrastructure.web.dto;

/**
 * Response body of the refund resource.
 *
 * <p>Fase 2.2 of docs/PLAN_API_WEB.md.</p>
 *
 * @param id      identifier of the refund
 * @param orderId order being refunded
 */
public record RefundResponse(Long id, Long orderId) {
}
