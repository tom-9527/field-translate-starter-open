package com.example.translate.spi;

import java.util.Collection;
import java.util.Map;

/**
 * 字典缓存访问 SPI。
 * <p>
 * 设计意图：隐藏具体缓存实现（本地/Redis 等），
 * 使翻译框架与缓存技术解耦。
 * </p>
 */
public interface DictCacheProvider {

    /**
     * 批量获取字典值。
     * <p>
     * 设计意图：强制批量访问，避免 N+1。
     * 缓存 miss 不应抛异常，仅返回命中的数据。
     * </p>
     *
     * @param dictKey 字典命名空间
     * @param codes 原始 code 集合
     * @return code -> 翻译值 映射
     */
    Map<Object, Object> getBatch(String dictKey, Collection<Object> codes);
}
