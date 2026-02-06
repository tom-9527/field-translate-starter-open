package com.example.translate.support;

/**
 * Cache key conventions for dictionary translation.
 * <p>
 * Design intent: make key formats explicit and consistent across cache
 * implementations without forcing any specific cache technology.
 * </p>
 */
public final class CacheKeySpec {

    /**
     * Prefix for dictionary entries.
     * <p>
     * Format: dict:{dictKey}:{code}
     * Example: dict:gender:1 -> "Male"
     * </p>
     */
    public static final String DICT_PREFIX = "dict";

    private CacheKeySpec() {
    }

    /**
     * Builds a dictionary cache key.
     * <p>
     * Design intent: centralize key construction so all caches follow the same
     * convention and can be swapped without data migration logic.
     * </p>
     *
     * @param dictKey dictionary namespace
     * @param code raw code value
     * @return cache key string
     */
    public static String dictKey(String dictKey, Object code) {
        return DICT_PREFIX + ":" + dictKey + ":" + String.valueOf(code);
    }
}
