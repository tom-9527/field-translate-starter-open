package com.example.translate.spi;

/**
 * 统一枚举接口（code -> desc）。
 * <p>
 * 设计意图：让枚举自身暴露翻译所需信息，
 * 框架不依赖任何业务枚举实现细节。
 * </p>
 *
 * @param <C> code 类型
 */
public interface CodeEnum<C> {

    /**
     * 获取枚举的 code。
     * <p>
     * 设计意图：保证翻译的 key 明确且稳定。
     * </p>
     *
     * @return code
     */
    C getCode();

    /**
     * 获取枚举的描述文案。
     * <p>
     * 设计意图：显示值由枚举本身管理。
     * </p>
     *
     * @return 描述
     */
    String getDesc();
}
