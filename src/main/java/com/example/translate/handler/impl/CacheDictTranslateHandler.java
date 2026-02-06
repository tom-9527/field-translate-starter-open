package com.example.translate.handler.impl;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;
import com.example.translate.handler.TranslateHandler;
import com.example.translate.spi.DictCacheProvider;
import com.example.translate.support.CacheKeySpec;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default handler for cache-based dictionary translation.
 * <p>
 * Design intent: delegate cache access to {@link DictCacheProvider} so the
 * framework remains agnostic of local/Redis implementations.
 * </p>
 */
public class CacheDictTranslateHandler implements TranslateHandler {

    private final DictCacheProvider cacheProvider;

    public CacheDictTranslateHandler(DictCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public TranslateType type() {
        return TranslateType.CACHE;
    }

    @Override
    public Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                              TranslateField meta,
                                              TranslateContext context) {
        if (rawValues == null || rawValues.isEmpty()) {
            return Collections.emptyMap();
        }

        String dictKey = meta.dictKey();
        if (dictKey == null || dictKey.isEmpty()) {
            // No dictionary namespace specified; degrade safely.
            return Collections.emptyMap();
        }

        if (cacheProvider == null) {
            // Cache provider not configured; degrade safely.
            return Collections.emptyMap();
        }

        Map<Object, Object> fromCache = safeGetBatch(dictKey, rawValues);
        if (fromCache == null || fromCache.isEmpty()) {
            return Collections.emptyMap();
        }

        // Normalize results to ensure only requested keys are returned.
        Map<Object, Object> result = new HashMap<>();
        for (Object rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            Object translated = fromCache.get(rawValue);
            if (translated != null) {
                result.put(rawValue, translated);
            }
        }
        return result;
    }

    private Map<Object, Object> safeGetBatch(String dictKey, Collection<Object> rawValues) {
        try {
            // DictCacheProvider is responsible for choosing a key format.
            // CacheKeySpec provides a recommended convention for consistency.
            return cacheProvider.getBatch(dictKey, rawValues);
        } catch (RuntimeException ex) {
            // Cache miss or failure must not break the main flow.
            return Collections.emptyMap();
        }
    }

    /**
     * Returns the recommended cache key format for one dictionary entry.
     * <p>
     * Design intent: make the key convention explicit for implementers.
     * </p>
     *
     * @param dictKey dictionary namespace
     * @param code raw code value
     * @return cache key string
     */
    public String buildCacheKey(String dictKey, Object code) {
        return CacheKeySpec.dictKey(dictKey, code);
    }
}
