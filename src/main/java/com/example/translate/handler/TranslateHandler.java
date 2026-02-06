package com.example.translate.handler;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SPI contract for a single translation strategy.
 * <p>
 * Design intent: each translation type (ENUM/CACHE/TABLE/RPC) provides one
 * implementation of this interface, enabling plug-and-play extension without
 * modifying framework code or business logic.
 * </p>
 */
public interface TranslateHandler {

    /**
     * Declares the translation type supported by this handler.
     * <p>
     * Design intent: allow the registry to route translation requests without
     * hardcoding conditional logic.
     * </p>
     *
     * @return supported translation type
     */
    TranslateType type();

    /**
     * Translates a batch of raw field values into display values.
     * <p>
     * Design intent: make batch translation the primary path to avoid N+1 lookups
     * and enable efficient cache/RPC/DB access patterns. The handler decides how
     * to interpret annotation metadata and execute the lookup, while the caller
     * only coordinates the flow.
     * </p>
     *
     * @param rawValues original field values from the DTO/VO collection
     * @param meta the annotation metadata that describes translation intent
     * @param context the current translation context for switches and policies
     * @return a map from raw value to translated value. Missing keys imply
     *         translation failure and should be treated as {@code null} or raw value.
     * @throws RuntimeException never thrown intentionally by design; implementations
     *                          must handle errors internally and degrade safely
     */
    Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                       TranslateField meta,
                                       TranslateContext context);

    /**
     * Translates a single raw field value into a display value.
     * <p>
     * Design intent: provide a convenience method while keeping batch translation
     * as the default execution model for performance.
     * </p>
     *
     * @param rawValue the original field value from the DTO/VO
     * @param meta the annotation metadata that describes translation intent
     * @param context the current translation context for switches and policies
     * @return translated value, or {@code null} / original value when translation fails
     */
    default Object translate(Object rawValue, TranslateField meta, TranslateContext context) {
        if (rawValue == null) {
            return null;
        }
        Map<Object, Object> result = batchTranslate(Collections.singleton(rawValue), meta, context);
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(rawValue);
    }
}
