package com.nexusmarket.domain.catalog;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Product offered in the NexusMarket catalog.
 *
 * <p>EXPLICIT source: component "bienes físicos o digitales ofrecidos".
 * Attributes: type (Físico/Digital), variants (list: color, talla, modelo,
 * etc.) and status (Publicado, Suspendido, Descontinuado).</p>
 *
 * <p>INTERPRETATION: {@code sellerId} is present because the seller
 * registers and administers "sus productos" (matriz de responsabilidades).
 * The specification does not describe the price, but the order increment needs
 * a current price to snapshot into the order lines (decision D-PRICE). Other
 * typical attributes (name, description...) remain intentionally omitted.</p>
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

    /** Current price; INTERPRETATION (D-PRICE). Zero means "not sellable yet". */
    private BigDecimal price = BigDecimal.ZERO.setScale(2);

    public Product() {
    }

    public Product(Long id, ProductType type, ProductStatus status) {
        this.id = id;
        this.type = type;
        this.status = status;
    }

    public Product(Long id, ProductType type, ProductStatus status, BigDecimal price) {
        this(id, type, status);
        setPrice(price);
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

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the current price; null means zero and negative values are rejected.
     *
     * @param price new price
     * @throws IllegalArgumentException if the price is negative
     */
    public void setPrice(BigDecimal price) {
        BigDecimal value = price == null ? BigDecimal.ZERO : price;
        if (value.signum() < 0) {
            throw new IllegalArgumentException("The price of a product can never be negative");
        }
        this.price = value.setScale(2, RoundingMode.HALF_UP);
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
