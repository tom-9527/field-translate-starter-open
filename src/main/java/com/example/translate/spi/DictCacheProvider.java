package com.example.translate.spi;

import java.util.Collection;
import java.util.Map;

/**
 * Abstraction for dictionary cache lookups.
 * <p>
 * Design intent: hide cache implementation details (local/Redis/etc.) behind a
 * simple contract so translation remains framework-agnostic.
 * </p>
 */
public interface DictCacheProvider {

    /**
     * Batch fetches dictionary values by key.
     * <p>
     * Design intent: allow efficient bulk lookups to avoid N+1 cache access.
     * Implementations should return only the hits and never throw on cache miss.
     * </p>
     *
     * @param dictKey dictionary namespace
     * @param codes collection of raw code values
     * @return map of code to translated value (missing keys imply cache miss)
     */
    Map<Object, Object> getBatch(String dictKey, Collection<Object> codes);
}
