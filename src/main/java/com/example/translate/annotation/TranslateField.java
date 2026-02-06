package com.example.translate.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 VO/DTO 字段为“需要翻译”的字段。
 * <p>
 * 设计意图：让翻译规则贴近数据模型声明，同时保持业务层完全无感知。
 * 该注解只声明“翻译意图”，自身不触发任何翻译逻辑。
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TranslateField {

    /**
     * 声明翻译类型，由 SPI 处理器根据类型进行路由。
     * <p>
     * 设计意图：新增翻译类型时无需修改注解或业务代码，
     * 仅通过注册新的处理器实现扩展。
     * </p>
     */
    TranslateType type();

    /**
     * 目标显示字段名，用于承接翻译后的值。
     * <p>
     * 设计意图：区分“原值字段”和“显示字段”，避免覆盖原始业务值。
     * </p>
     */
    String target() default "";

    /**
     * 枚举翻译时使用的枚举类。
     * <p>
     * 设计意图：让枚举自身描述 code->desc 关系，
     * 框架不硬编码任何业务枚举。
     * </p>
     */
    Class<? extends Enum<?>> enumClass() default NoneEnum.class;

    /**
     * 缓存字典翻译时使用的字典 key / code。
     * <p>
     * 设计意图：通过稳定的字典命名空间进行翻译，
     * 不绑定具体缓存实现。
     * </p>
     */
    String dictKey() default "";

    /**
     * 表字段翻译时使用的表名。
     * <p>
     * 设计意图：支持轻量表查询，避免在业务 SQL 中强制 JOIN。
     * </p>
     */
    String table() default "";

    /**
     * 表字段翻译时用于匹配的“键列”。
     * <p>
     * 设计意图：让不同表结构都可被框架复用，无需改动框架代码。
     * </p>
     */
    String keyColumn() default "";

    /**
     * 表字段翻译时用于展示的“值列”。
     * <p>
     * 设计意图：显示字段可配置，避免与实体映射耦合。
     * </p>
     */
    String valueColumn() default "";

    /**
     * RPC 翻译时的服务标识。
     * <p>
     * 设计意图：不绑定具体 RPC 框架，仅声明路由信息。
     * </p>
     */
    String rpcService() default "";

    /**
     * RPC 翻译时的方法/操作名称。
     * <p>
     * 设计意图：把调用细节交给 SPI 实现，注解只负责描述意图。
     * </p>
     */
    String rpcMethod() default "";

    /**
     * 预留扩展参数。
     * <p>
     * 设计意图：提供向后兼容的扩展位，减少频繁修改注解属性。
     * </p>
     */
    String param() default "";

    /**
     * 翻译失败时使用的兜底值。
     * <p>
     * 设计意图：保证翻译失败不影响主流程，支持业务可控降级。
     * </p>
     */
    String fallback() default "";
}
