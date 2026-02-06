package com.example.translate.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 请求级翻译上下文，存储于 ThreadLocal。
 * <p>
 * 设计意图：提供最小且框架无关的开关控制能力，
 * 使其可在任何 Web 栈中使用且不依赖 AOP。
 * 同时明确区分“全局开关”和“请求级开关”，
 * 以保证行为可预期且易测试。
 * </p>
 */
public final class TranslateContext {

    private static final ThreadLocal<TranslateContext> LOCAL = new ThreadLocal<>();

    /**
     * 全局开关，控制翻译是否默认启用。
     * <p>
     * 设计意图：支持配置化启停，且不触及业务代码。
     * 该值为进程级默认策略。
     * </p>
     */
    private static volatile boolean globalEnabled = true;

    private boolean enabled;
    private final Set<String> enabledTypes;

    private TranslateContext(boolean enabled, Set<String> enabledTypes) {
        this.enabled = enabled;
        this.enabledTypes = enabledTypes == null ? new HashSet<>() : new HashSet<>(enabledTypes);
    }

    /**
     * 获取当前线程的上下文；若不存在则创建默认上下文。
     * <p>
     * 设计意图：避免调用方到处判空，同时保证每个请求可独立覆盖策略。
     * </p>
     *
     * @return 当前线程上下文
     */
    public static TranslateContext current() {
        TranslateContext ctx = LOCAL.get();
        if (ctx == null) {
            // 使用全局策略创建默认上下文
            ctx = new TranslateContext(globalEnabled, null);
            LOCAL.set(ctx);
        }
        return ctx;
    }

    /**
     * 清理当前线程上下文。
     * <p>
     * 设计意图：避免线程池复用导致的 ThreadLocal 泄漏。
     * </p>
     */
    public static void clear() {
        LOCAL.remove();
    }

    /**
     * 设置全局启用开关。
     * <p>
     * 设计意图：集中式开关，便于配置驱动。
     * </p>
     *
     * @param enabled 是否全局启用
     */
    public static void setGlobalEnabled(boolean enabled) {
        globalEnabled = enabled;
    }

    /**
     * 获取全局启用开关。
     *
     * @return 是否全局启用
     */
    public static boolean isGlobalEnabled() {
        return globalEnabled;
    }

    /**
     * 设置当前线程是否启用翻译。
     * <p>
     * 设计意图：允许请求级别的开关控制，且不影响其他线程。
     * </p>
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 判断当前线程是否启用翻译。
     * <p>
     * 设计意图：全局关闭优先生效，保证统一安全策略。
     * </p>
     *
     * @return 是否启用翻译
     */
    public boolean isEnabled() {
        return globalEnabled && enabled;
    }

    /**
     * 设置当前线程允许的翻译类型集合。
     * <p>
     * 设计意图：为“仅开启部分翻译类型”预留空间。
     * 空集合表示不做类型过滤。
     * </p>
     *
     * @param types 允许的类型标识集合
     */
    public void setEnabledTypes(Set<String> types) {
        enabledTypes.clear();
        if (types != null) {
            enabledTypes.addAll(types);
        }
    }

    /**
     * 获取当前线程允许的翻译类型集合（只读）。
     * <p>
     * 设计意图：避免调用方误修改内部集合。
     * </p>
     *
     * @return 允许的类型集合
     */
    public Set<String> getEnabledTypes() {
        return Collections.unmodifiableSet(enabledTypes);
    }

    /**
     * 判断某个翻译类型是否被允许。
     * <p>
     * 设计意图：将策略判断集中在上下文中，
     * 使 handler/registry 保持无状态与简洁。
     * </p>
     *
     * @param type 类型标识
     * @return 是否允许
     */
    public boolean isTypeEnabled(String type) {
        if (!isEnabled()) {
            return false;
        }
        if (enabledTypes.isEmpty()) {
            // 空集合表示不做过滤，全部允许
            return true;
        }
        return Objects.nonNull(type) && enabledTypes.contains(type);
    }
}
