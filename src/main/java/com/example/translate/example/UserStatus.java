package com.example.translate.example;

import com.example.translate.spi.CodeEnum;

/**
 * 枚举翻译示例枚举。
 * <p>
 * 设计意图：由枚举自身维护 code 到展示文本的映射。
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
