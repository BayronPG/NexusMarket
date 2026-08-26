package com.nexusmarket.domain.catalog;

import java.util.ArrayList;
import java.util.List;

/**
 * Product offered in the NexusMarket catalog.
 *
 * <p>EXPLICIT source: component "bienes físicos o digitales ofrecidos".
 * Attributes: type (Físico/Digital), variants (list: color, talla, modelo,
 * etc.) and status (Publicado, Suspendido, Descontinuado).</p>
 *
 * <p>INTERPRETATION: {@code sellerId} is present because the seller
 * registers and administers "sus productos" (matriz de responsabilidades).
 * Other typical product attributes (name, price, description...) are NOT in
 * the specification and are intentionally omitted (PENDING).</p>
 *
 * <p>Business rules: physical products require inventory and shipping
 * (RN-07); digital products are delivered immediately after payment
 * (RN-08).</p>
 *
 * <p>Traceability: OBJ-05, RN-06, RN-07, RN-08.</p>
 */
public class Product {

    /** Unique identifier of the product. */
    private Long id;

    /** Seller that registers the product; INTERPRETATION. */
    private Long sellerId;

    /** Physical or digital; required. */
    private ProductType type;

    /** Differences such as color, size, model; list. Nature PENDING. */
    private List<String> variants = new ArrayList<>();

    /** Publication status; required. */
    private ProductStatus status;

    public Product() {
    }

    public Product(Long id, ProductType type, ProductStatus status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    /** Whether the product is physical (RN-07). */
    public boolean isPhysical() {
        return type == ProductType.PHYSICAL;
    }

    /** Whether the product is digital (RN-08). */
    public boolean isDigital() {
        return type == ProductType.DIGITAL;
    }
}
