package com.example.translate.support;

/**
 * 缓存字典的 key 规范。
 * <p>
 * 设计意图：明确 key 格式，保证不同缓存实现之间的一致性，
 * 同时不绑定任何具体缓存技术。
 * </p>
 */
public final class CacheKeySpec {

    /**
     * 字典缓存前缀。
     * <p>
     * 格式：dict:{dictKey}:{code}
     * 示例：dict:gender:1 -> "Male"
     * </p>
     */
    public static final String DICT_PREFIX = "dict";

    private CacheKeySpec() {
    }

    /**
     * 构建字典缓存 key。
     * <p>
     * 设计意图：集中构建规则，避免不同模块使用不同格式。
     * </p>
     *
     * @param dictKey 字典命名空间
     * @param code 原始 code
     * @return 缓存 key
     */
    public static String dictKey(String dictKey, Object code) {
        return DICT_PREFIX + ":" + dictKey + ":" + String.valueOf(code);
    }
}
