package com.example.translate.handler;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 单一翻译策略的 SPI 接口。
 * <p>
 * 设计意图：每种翻译方式（ENUM/CACHE/TABLE/RPC）实现该接口，
 * 以实现可插拔扩展而无需修改核心逻辑。
 * </p>
 */
public interface TranslateHandler {

    /**
     * 返回当前处理器支持的翻译类型。
     * <p>
     * 设计意图：由注册表进行路由，避免硬编码条件判断。
     * </p>
     *
     * @return 翻译类型
     */
    TranslateType type();

    /**
     * 批量翻译入口。
     * <p>
     * 设计意图：以批量为默认路径避免 N+1 查询，
     * 便于缓存/DB/RPC 的高效访问。
     * </p>
     *
     * @param rawValues 原始字段值集合
     * @param meta 注解元信息（翻译意图）
     * @param context 当前翻译上下文（开关/策略）
     * @return 原值到翻译值的映射；缺失键表示翻译失败
     * @throws RuntimeException 设计上不应抛出，
     *                          实现需自行吞错并降级
     */
    Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                       TranslateField meta,
                                       TranslateContext context);

    /**
     * 单值翻译便捷方法。
     * <p>
     * 设计意图：提供易用接口，但仍以批量逻辑为核心。
     * </p>
     *
     * @param rawValue 原始字段值
     * @param meta 注解元信息（翻译意图）
     * @param context 当前翻译上下文（开关/策略）
     * @return 翻译值；失败时返回 null 或原值
     */
    default Object translate(Object rawValue, TranslateField meta, TranslateContext context) {
        if (rawValue == null) {
            return null;
        }
        Map<Object, Object> result = batchTranslate(Collections.singleton(rawValue), meta, context);
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(rawValue);
    }
}
