package com.nexusmarket.domain.catalog;

/**
 * Publication status of a product.
 *
 * <p>EXPLICIT source: "Publicado, Suspendido o Descontinuado" (selección).</p>
 *
 * <p>Traceability: OBJ-05.</p>
 */
public enum ProductStatus {
    /** Product is visible in the public catalog. */
    PUBLISHED,
    /** Product is temporarily not offered. */
    SUSPENDED,
    /** Product is no longer offered. */
    DISCONTINUED
}
