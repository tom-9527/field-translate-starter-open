package com.example.translate.registry;

import com.example.translate.annotation.TranslateType;
import com.example.translate.handler.TranslateHandler;

/**
 * 翻译处理器注册表。
 * <p>
 * 设计意图：提供统一查找入口，使调用方无需关心
 * handler 的创建方式或注册细节。
 * </p>
 */
public interface TranslateHandlerRegistry {

    /**
     * 获取指定类型的处理器。
     * <p>
     * 设计意图：新增翻译类型时无需修改调用方逻辑，
     * 仅注册新的 handler 即可。
     * </p>
     *
     * @param type 翻译类型
     * @return 处理器实例；不存在时返回 {@code null}
     */
    TranslateHandler getHandler(TranslateType type);
}
