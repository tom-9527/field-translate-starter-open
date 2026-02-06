package com.example.translate.handler.impl;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;
import com.example.translate.handler.TranslateHandler;
import com.example.translate.spi.RpcTranslateClient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 RPC 翻译的默认处理器。
 * <p>
 * 设计意图：将真实的 RPC 调用委托给 {@link RpcTranslateClient}，
 * 使框架与 Feign/Dubbo 等实现保持解耦。
 * </p>
 */
public class RpcTranslateHandler implements TranslateHandler {

    private final RpcTranslateClient client;

    public RpcTranslateHandler(RpcTranslateClient client) {
        this.client = client;
    }

    @Override
    public TranslateType type() {
        return TranslateType.RPC;
    }

    @Override
    public Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                              TranslateField meta,
                                              TranslateContext context) {
        if (rawValues == null || rawValues.isEmpty()) {
            return Collections.emptyMap();
        }
        if (client == null) {
            // 未配置客户端，安全降级。
            return Collections.emptyMap();
        }

        String service = meta.rpcService();
        String method = meta.rpcMethod();
        String param = meta.param();
        if (service == null || service.isEmpty()) {
            // 缺少路由信息，安全降级。
            return Collections.emptyMap();
        }

        Map<Object, Object> fetched = safeFetch(service, method, rawValues, param);
        if (fetched == null || fetched.isEmpty()) {
            return Collections.emptyMap();
        }

        // 规范化结果，确保只返回请求的键。
        Map<Object, Object> result = new HashMap<>();
        for (Object rawValue : rawValues) {
            if (rawValue == null) {
                continue;
            }
            Object translated = fetched.get(rawValue);
            if (translated != null) {
                result.put(rawValue, translated);
            }
        }
        return result;
    }

    private Map<Object, Object> safeFetch(String service,
                                          String method,
                                          Collection<Object> rawValues,
                                          String param) {
        try {
            // 客户端负责处理超时/重试并可返回部分结果。
            return client.batchFetch(service, method, rawValues, param);
        } catch (RuntimeException ex) {
            // RPC 失败不应影响主流程。
            return Collections.emptyMap();
        }
    }
}
