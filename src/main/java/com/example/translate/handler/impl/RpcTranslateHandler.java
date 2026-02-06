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
 * Default handler for RPC-based translation.
 * <p>
 * Design intent: delegate the actual RPC call to {@link RpcTranslateClient}
 * so the framework remains independent of Feign/Dubbo/etc.
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
            // Client not configured; degrade safely.
            return Collections.emptyMap();
        }

        String service = meta.rpcService();
        String method = meta.rpcMethod();
        String param = meta.param();
        if (service == null || service.isEmpty()) {
            // Missing routing info; degrade safely.
            return Collections.emptyMap();
        }

        Map<Object, Object> fetched = safeFetch(service, method, rawValues, param);
        if (fetched == null || fetched.isEmpty()) {
            return Collections.emptyMap();
        }

        // Normalize results to ensure only requested keys are returned.
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
            // Client handles timeouts/retries and returns partial results.
            return client.batchFetch(service, method, rawValues, param);
        } catch (RuntimeException ex) {
            // RPC failures must not break the main flow.
            return Collections.emptyMap();
        }
    }
}
