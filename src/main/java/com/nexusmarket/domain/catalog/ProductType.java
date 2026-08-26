package com.nexusmarket.domain.catalog;

/**
 * Type of a product.
 *
 * <p>EXPLICIT source: "Tipo de Producto: Físico o Digital" (selección).</p>
 *
 * <p>Traceability: OBJ-05, RN-06, RN-07, RN-08.</p>
 */
public enum ProductType {
    /** Physical product: requires inventory and shipping. */
    PHYSICAL,
    /** Digital product: delivered immediately after payment. */
    DIGITAL
}
