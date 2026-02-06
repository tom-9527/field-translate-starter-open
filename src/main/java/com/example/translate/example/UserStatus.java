package com.example.translate.example;

import com.example.translate.spi.CodeEnum;

/**
 * Example enum for demonstrating enum translation.
 * <p>
 * Design intent: the enum itself owns the mapping from code to display text.
 * </p>
 */
public enum UserStatus implements CodeEnum<Integer> {

    DISABLED(0, "Disabled"),
    ENABLED(1, "Enabled");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
