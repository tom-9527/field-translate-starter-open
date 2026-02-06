package com.example.translate.support;

/**
 * 返回前翻译的执行入口。
 * <p>
 * 设计意图：让触发机制与遍历/翻译逻辑解耦，
 * 便于在不同框架中复用。
 * </p>
 */
public interface TranslateExecutor {

    /**
     * 对响应对象执行翻译（原地修改）。
     * <p>
     * 设计意图：不改变控制器返回签名，
     * 但在返回边界统一应用翻译规则。
     * </p>
     *
     * @param body 响应对象
     * @return 原对象实例
     */
    Object translate(Object body);
}
