package com.example.translate.spi;

/**
 * Optional contract for enums that expose a code and description pair.
 * <p>
 * Design intent: provide a uniform way to map code -> description without
 * hardcoding enum details inside the framework.
 * </p>
 *
 * @param <C> code type
 */
public interface CodeEnum<C> {

    /**
     * Returns the code used for translation lookup.
     * <p>
     * Design intent: keep translation keys explicit and stable.
     * </p>
     *
     * @return code value
     */
    C getCode();

    /**
     * Returns the human-readable description.
     * <p>
     * Design intent: keep display values owned by the enum itself.
     * </p>
     *
     * @return description
     */
    String getDesc();
}
