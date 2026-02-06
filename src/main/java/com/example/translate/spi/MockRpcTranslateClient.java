package com.example.translate.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock RPC 客户端，仅用于演示/测试。
 * <p>
 * 设计意图：提供最小示例，生产环境请替换为真实 RPC 实现。
 * </p>
 */
public class MockRpcTranslateClient implements RpcTranslateClient {

    @Override
    public Map<Object, Object> batchFetch(String service,
                                          String method,
                                          Collection<Object> codes,
                                          String param) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 模拟 RPC 返回：code -> "RPC:" + code
        Map<Object, Object> result = new HashMap<>();
        for (Object code : codes) {
            if (code == null) {
                continue;
            }
            result.put(code, "RPC:" + String.valueOf(code));
        }
        return result;
    }
}
