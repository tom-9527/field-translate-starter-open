package com.example.translate.registry;

import com.example.translate.annotation.TranslateType;
import com.example.translate.handler.TranslateHandler;

/**
 * Registry for translation handlers.
 * <p>
 * Design intent: provide a single lookup point so callers never need to know
 * how handlers are created or wired. This keeps routing logic centralized and
 * avoids hardcoded conditionals in the translation flow.
 * </p>
 */
public interface TranslateHandlerRegistry {

    /**
     * Returns the handler for a given translation type.
     * <p>
     * Design intent: allow new translation types to be registered without
     * changing caller logic or adding switch/if-else statements.
     * </p>
     *
     * @param type translation type
     * @return matching handler, or {@code null} if none is registered
     */
    TranslateHandler getHandler(TranslateType type);
}
