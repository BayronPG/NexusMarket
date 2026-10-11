package com.nexusmarket.application.service;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.application.Authorization;
import com.nexusmarket.application.Validation;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.ManageOrdersUseCase;
import com.nexusmarket.application.port.out.BuyerRepository;
import com.nexusmarket.application.port.out.OrderRepository;
import com.nexusmarket.domain.buyer.Buyer;
import com.nexusmarket.domain.order.Order;
import com.nexusmarket.domain.order.OrderStatus;
import com.nexusmarket.domain.user.Role;
import org.springframework.stereotype.Service;

/**
 * Application service that manages the order lifecycle.
 *
 * <p>EXPLICIT source: the Buyer, the Seller and the Logistics Operator manage
 * orders (responsibilities matrix); an order follows the cycle Carrito,
 * Pendiente de Pago, Pagado, Despachado, Entregado/Finalizado without extra
 * transitions (RN-16); a finalized order cannot be modified (RN-13).</p>
 *
 * <p>INTERPRETATION: a buyer can only operate on its own orders (RG-03). The
 * lifecycle rules themselves stay in {@link Order#changeStatus(OrderStatus)}.</p>
 *
 * <p>Traceability: OBJ-07, OBJ-08, RN-13, RN-16, RG-03.</p>
 */
@Service
public class OrderService implements ManageOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;

    public OrderService(OrderRepository orderRepository, BuyerRepository buyerRepository) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
    }

    @Override
    public Order createOrder(ActorContext actor) {
        Authorization.requireAnyRole(actor, Role.BUYER);
        Buyer buyer = requireBuyerOfActor(actor);
        Order order = new Order(null, buyer.getId(), OrderStatus.CART);
        return orderRepository.save(order);
    }

    @Override
    public Order changeOrderStatus(ActorContext actor, Long orderId, OrderStatus next) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR);
        Validation.requireNotNull(next, "next status");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (actor.role() == Role.BUYER) {
            Buyer buyer = requireBuyerOfActor(actor);
            if (!buyer.getId().equals(order.getBuyerId())) {
                throw new AuthorizationException("A buyer can only manage its own orders (RG-03)");
            }
        }

        order.changeStatus(next);
        return orderRepository.save(order);
    }

    @Override
    public Order findOrder(ActorContext actor, Long orderId) {
        Authorization.requireAnyRole(actor, Role.BUYER, Role.SELLER, Role.LOGISTICS_OPERATOR);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    private Buyer requireBuyerOfActor(ActorContext actor) {
        return buyerRepository.findByUserId(actor.userId())
                .orElseThrow(() -> new NotFoundException("Buyer not found for the authenticated user"));
    }
}
