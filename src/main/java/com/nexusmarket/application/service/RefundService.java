package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageRefundsUseCase;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.application.port.out.RefundRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.returns.Refund;
import com.nexusmarket.domain.user.Role;
import org.springframework.stereotype.Service;

/**
 * Application service that manages refunds.
 *
 * <p>EXPLICIT source: refund management is a responsibility of the Buyer and
 * the Administrator (responsibilities matrix, OBJ-11).</p>
 *
 * <p>INTERPRETATION: a buyer can only request refunds for its own orders
 * (RG-03). The attributes of a refund are PENDING in the source, so only the
 * association with its order is registered.</p>
 *
 * <p>Traceability: OBJ-11, RG-03.</p>
 */
@Service
public class RefundService implements ManageRefundsUseCase {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;

    public RefundService(RefundRepository refundRepository,
                         OrderRepository orderRepository,
                         BuyerRepository buyerRepository) {
        this.refundRepository = refundRepository;
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
    }

    @Override
    public Refund requestRefund(ActorContext actor, RequestRefundCommand command) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.ADMINISTRATOR);
        Validation.requireNotNull(command, "command");
        Long orderId = Validation.requireNotNull(command.orderId(), "orderId");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (actor.role() == Role.BUYER) {
            Buyer buyer = buyerRepository.findByUserId(actor.userId())
                    .orElseThrow(() -> new NotFoundException("Buyer not found for the authenticated user"));
            if (!buyer.getId().equals(order.getBuyerId())) {
                throw new AuthorizationException("A buyer can only request refunds for its own orders (RG-03)");
            }
        }

        Refund refund = new Refund();
        refund.setOrderId(order.getId());
        return refundRepository.save(refund);
    }
}
