package com.example.translate.annotation;

/**
 * Supported translation types recognized by the framework.
 * <p>
 * Design intent: provide a stable, declarative contract for selecting SPI
 * handlers without encoding any business-specific logic.
 * </p>
 */
public enum TranslateType {

    /**
     * Enum-based translation (local enum mapping).
     */
    ENUM,

    /**
     * Cache-based dictionary translation.
     */
    CACHE,

    /**
     * Table-based translation (light lookup, no joins).
     */
    TABLE,

    /**
     * RPC-based translation (external service call).
     */
    RPC
}
