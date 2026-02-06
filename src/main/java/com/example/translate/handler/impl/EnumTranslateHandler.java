package com.example.translate.handler.impl;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;
import com.example.translate.handler.TranslateHandler;
import com.example.translate.spi.CodeEnum;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default handler for enum-based translation.
 * <p>
 * Design intent: use enum metadata declared on {@link TranslateField} to map
 * codes to descriptions without tying the framework to any business enum.
 * </p>
 */
public class EnumTranslateHandler implements TranslateHandler {

    @Override
    public TranslateType type() {
        return TranslateType.ENUM;
    }

    @Override
    public Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                              TranslateField meta,
                                              TranslateContext context) {
        if (rawValues == null || rawValues.isEmpty()) {
            return Collections.emptyMap();
        }

        Class<? extends Enum<?>> enumClass = meta.enumClass();
        if (enumClass == null || enumClass == Enum.class) {
            // No enum class specified; degrade safely.
            return Collections.emptyMap();
        }

        Map<Object, Object> result = new HashMap<>();
        Enum<?>[] constants = enumClass.getEnumConstants();
        if (constants == null || constants.length == 0) {
            return result;
        }

        for (Object rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            Object translated = resolveEnum(rawValue, constants);
            if (translated != null) {
                result.put(rawValue, translated);
            }
        }

        return result;
    }

    private Object resolveEnum(Object rawValue, Enum<?>[] constants) {
        for (Enum<?> constant : constants) {
            // Prefer CodeEnum contract for explicit code->desc mapping.
            if (constant instanceof CodeEnum) {
                Object code = ((CodeEnum<?>) constant).getCode();
                if (rawValue.equals(code)) {
                    return ((CodeEnum<?>) constant).getDesc();
                }
                continue;
            }

            // Fallback: match by name or ordinal string.
            if (rawValue instanceof String) {
                if (constant.name().equals(rawValue)) {
                    return constant.name();
                }
            }
            if (rawValue instanceof Number) {
                int ordinal = ((Number) rawValue).intValue();
                if (constant.ordinal() == ordinal) {
                    return constant.name();
                }
            }
        }
        return null;
    }
}
