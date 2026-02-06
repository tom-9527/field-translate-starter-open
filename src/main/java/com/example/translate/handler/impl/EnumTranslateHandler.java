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
 * 基于枚举翻译的默认处理器。
 * <p>
 * 设计意图：使用 {@link TranslateField} 上声明的枚举元信息完成
 * code 到描述的映射，框架不绑定任何业务枚举实现。
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
            // 未指定枚举类，安全降级。
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
            // 优先使用 CodeEnum 约定的 code->desc 映射。
            if (constant instanceof CodeEnum) {
                Object code = ((CodeEnum<?>) constant).getCode();
                if (rawValue.equals(code)) {
                    return ((CodeEnum<?>) constant).getDesc();
                }
                continue;
            }

            // 兜底：按名称或序号字符串匹配。
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
