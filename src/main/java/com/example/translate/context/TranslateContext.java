package com.example.translate.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Request-scoped translation context stored in a ThreadLocal.
 * <p>
 * Design intent: provide a minimal, framework-agnostic control surface that
 * can be safely used in any web stack without AOP dependencies.
 * It explicitly separates global enablement from per-thread overrides to keep
 * translation behavior predictable and testable.
 * </p>
 */
public final class TranslateContext {

    private static final ThreadLocal<TranslateContext> LOCAL = new ThreadLocal<>();

    /**
     * Global switch controlling whether translation is enabled by default.
     * <p>
     * Design intent: allow runtime or configuration-based toggling without
     * touching business code. This value is intentionally static to represent
     * a process-wide default policy.
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
     * Returns the current thread's context, creating one if absent.
     * <p>
     * Design intent: avoid null checks in callers and ensure each request can
     * safely override translation behavior without global side effects.
     * </p>
     *
     * @return current thread context
     */
    public static TranslateContext current() {
        TranslateContext ctx = LOCAL.get();
        if (ctx == null) {
            // Create a default context that inherits the global policy
            ctx = new TranslateContext(globalEnabled, null);
            LOCAL.set(ctx);
        }
        return ctx;
    }

    /**
     * Clears the current thread's context.
     * <p>
     * Design intent: prevent ThreadLocal leaks in thread pools and guarantee
     * clean boundaries between requests.
     * </p>
     */
    public static void clear() {
        LOCAL.remove();
    }

    /**
     * Sets the global enablement flag.
     * <p>
     * Design intent: allow configuration-driven changes while keeping the
     * decision centralized and explicit.
     * </p>
     *
     * @param enabled whether translation is globally enabled
     */
    public static void setGlobalEnabled(boolean enabled) {
        globalEnabled = enabled;
    }

    /**
     * Returns the current global enablement flag.
     *
     * @return true if globally enabled
     */
    public static boolean isGlobalEnabled() {
        return globalEnabled;
    }

    /**
     * Enables or disables translation for the current thread.
     * <p>
     * Design intent: allow request-level overrides without affecting other
     * threads or the global default.
     * </p>
     *
     * @param enabled whether translation is enabled for this thread
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether translation is enabled for the current thread.
     * <p>
     * Design intent: always combine thread state with global policy to maintain
     * a predictable contract (global off always wins).
     * </p>
     *
     * @return true if translation should run for this thread
     */
    public boolean isEnabled() {
        return globalEnabled && enabled;
    }

    /**
     * Replaces the set of enabled translation types for this thread.
     * <p>
     * Design intent: allow future selective enabling by type while defaulting
     * to "all types" when empty.
     * </p>
     *
     * @param types set of enabled type identifiers
     */
    public void setEnabledTypes(Set<String> types) {
        enabledTypes.clear();
        if (types != null) {
            enabledTypes.addAll(types);
        }
    }

    /**
     * Returns an immutable view of enabled translation types for this thread.
     * <p>
     * Design intent: expose safe read-only data to avoid accidental mutation
     * during translation.
     * </p>
     *
     * @return enabled type identifiers
     */
    public Set<String> getEnabledTypes() {
        return Collections.unmodifiableSet(enabledTypes);
    }

    /**
     * Checks whether a given translation type is enabled for this thread.
     * <p>
     * Design intent: keep the selection policy localized so handlers and
     * registries can remain simple and stateless.
     * </p>
     *
     * @param type type identifier
     * @return true if type is enabled or no explicit filter is set
     */
    public boolean isTypeEnabled(String type) {
        if (!isEnabled()) {
            return false;
        }
        if (enabledTypes.isEmpty()) {
            // Empty means "no filter", so all types are allowed
            return true;
        }
        return Objects.nonNull(type) && enabledTypes.contains(type);
    }
}
