package com.example.translate.support;

/**
 * Entry point for translating response objects before they are serialized.
 * <p>
 * Design intent: keep the trigger mechanism decoupled from traversal logic,
 * making it easy to swap or wrap the executor in different environments.
 * </p>
 */
public interface TranslateExecutor {

    /**
     * Translates a response body in place.
     * <p>
     * Design intent: avoid altering controller signatures while still allowing
     * translation to be applied consistently at the response boundary.
     * </p>
     *
     * @param body response body to translate
     * @return the same body instance for chaining
     */
    Object translate(Object body);
}
