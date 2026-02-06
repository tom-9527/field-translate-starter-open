package com.example.translate.annotation;

/**
 * 框架识别的翻译类型。
 * <p>
 * 设计意图：为 SPI 处理器提供稳定的类型标识，
 * 以避免硬编码或散落的条件判断。
 * </p>
 */
public enum TranslateType {

    /**
     * 枚举翻译（本地枚举映射）。
     */
    ENUM,

    /**
     * 缓存字典翻译。
     */
    CACHE,

    /**
     * 表字段翻译（轻量查询）。
     */
    TABLE,

    /**
     * RPC 翻译（外部服务）。
     */
    RPC
}
