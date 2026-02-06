package com.example.translate.spi;

import java.util.Collection;
import java.util.Map;

/**
 * SPI for RPC-based translation.
 * <p>
 * Design intent: decouple the translation framework from any specific RPC
 * technology while still enabling batch lookups.
 * </p>
 */
public interface RpcTranslateClient {

    /**
     * Batch fetches translated values from an external service.
     * <p>
     * Design intent: enforce batch access to prevent N+1 network calls.
     * Implementations must handle timeouts/errors internally and return
     * partial results without throwing.
     * </p>
     *
     * @param service service identifier (from annotation)
     * @param method method or operation name (from annotation)
     * @param codes raw code values
     * @param param optional custom parameter (from annotation)
     * @return map of code to translated value (missing keys imply failure)
     */
    Map<Object, Object> batchFetch(String service,
                                   String method,
                                   Collection<Object> codes,
                                   String param);
}
