package com.example.translate.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a DTO/VO field as translatable during the response phase.
 * <p>
 * Design intent: keep translation rules close to the data model while ensuring
 * business layers remain unaware of translation mechanics. This annotation is
 * purely declarative and must not trigger any translation by itself.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TranslateField {

    /**
     * Declares the translation strategy type to be resolved by SPI handlers.
     * <p>
     * Design intent: allow new translation types without changing this annotation
     * or business code; handlers are registered and matched by type.
     * </p>
     */
    TranslateType type();

    /**
     * The target display field name to receive the translated value.
     * <p>
     * Design intent: separate raw value and display value without altering
     * the original field, preventing accidental overwrites.
     * </p>
     */
    String target() default "";

    /**
     * Enum class used for enum-based translation.
     * <p>
     * Design intent: keep enum translation self-describing without hardcoding
     * any business enum in the framework.
     * </p>
     */
    Class<? extends Enum<?>> enumClass() default Enum.class;

    /**
     * Dictionary key/code used for cache-based translation.
     * <p>
     * Design intent: allow a cache-backed dictionary to be referenced by a
     * stable key without binding to a specific cache implementation.
     * </p>
     */
    String dictKey() default "";

    /**
     * Table name used for table-based translation.
     * <p>
     * Design intent: allow lightweight table lookup without enforcing joins or
     * ORM details in business logic.
     * </p>
     */
    String table() default "";

    /**
     * Column name representing the source code/key in table-based translation.
     * <p>
     * Design intent: keep lookup key configurable to support different schemas
     * without framework changes.
     * </p>
     */
    String keyColumn() default "";

    /**
     * Column name representing the display/value in table-based translation.
     * <p>
     * Design intent: keep returned display value configurable and decoupled from
     * entity mappings.
     * </p>
     */
    String valueColumn() default "";

    /**
     * RPC identifier or service name used for RPC-based translation.
     * <p>
     * Design intent: allow translation via external services without binding to
     * a specific RPC framework or transport.
     * </p>
     */
    String rpcService() default "";

    /**
     * Optional RPC method or operation name for RPC-based translation.
     * <p>
     * Design intent: keep invocation details configurable by annotation while
     * leaving the actual call to SPI handlers.
     * </p>
     */
    String rpcMethod() default "";

    /**
     * Optional parameter to pass to handlers (reserved for future extension).
     * <p>
     * Design intent: provide a forward-compatible slot for custom behavior
     * without introducing new annotation attributes.
     * </p>
     */
    String param() default "";

    /**
     * Fallback value used when translation fails.
     * <p>
     * Design intent: allow safe degradation without throwing exceptions or
     * affecting the main response.
     * </p>
     */
    String fallback() default "";
}
