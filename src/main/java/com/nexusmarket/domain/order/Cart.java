package com.nexusmarket.domain.order;

import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart of a buyer.
 *
 * <p>EXPLICIT source: "Carrito: Selección provisional de productos"
 * (OBJ-07); it is the initial state of the order lifecycle.</p>
 *
 * <p>INTERPRETATION: the content of the cart (products, quantities, prices)
 * is not described by the specification; {@code productIds} is a provisional
 * representation and its definitive structure is PENDING.</p>
 *
 * <p>Traceability: OBJ-07.</p>
 */
public class Cart {

    /** Unique identifier of the cart. */
    private Long id;

    /** Buyer owner of the cart; INTERPRETATION. */
    private Long buyerId;

    /** Provisional selection of products; INTERPRETATION/PENDING. */
    private List<Long> productIds = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long id, Long buyerId) {
        this.id = id;
        this.buyerId = buyerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
