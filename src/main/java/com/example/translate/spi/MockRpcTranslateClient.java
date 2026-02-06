package com.example.translate.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock RPC client implementation for demonstration/testing.
 * <p>
 * Design intent: provide a minimal example without binding to any RPC stack.
 * This class should be replaced by a real client in production.
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

        // Simulate a remote lookup by returning code->"RPC:"+code mappings.
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
