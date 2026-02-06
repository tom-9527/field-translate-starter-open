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
 * 基于缓存字典翻译的默认处理器。
 * <p>
 * 设计意图：把缓存访问委托给 {@link DictCacheProvider}，
 * 使框架对本地缓存/Redis 等实现保持无感知。
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
            // 未指定字典命名空间，安全降级。
            return Collections.emptyMap();
        }

        if (cacheProvider == null) {
            // 未配置缓存提供者，安全降级。
            return Collections.emptyMap();
        }

        Map<Object, Object> fromCache = safeGetBatch(dictKey, rawValues);
        if (fromCache == null || fromCache.isEmpty()) {
            return Collections.emptyMap();
        }

        // 规范化结果，确保只返回请求的键。
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
            // DictCacheProvider 负责选择 key 格式。
            // CacheKeySpec 提供推荐规范以保持一致性。
            return cacheProvider.getBatch(dictKey, rawValues);
        } catch (RuntimeException ex) {
            // 缓存未命中或异常不应影响主流程。
            return Collections.emptyMap();
        }
    }

    /**
     * 返回单条字典的推荐缓存 key 格式。
     * <p>
     * 设计意图：把 key 约定显式化，便于实现者遵循一致规范。
     * </p>
     *
     * @param dictKey 字典命名空间
     * @param code 原始 code 值
     * @return 缓存 key 字符串
     */
    public String buildCacheKey(String dictKey, Object code) {
        return CacheKeySpec.dictKey(dictKey, code);
    }
}
