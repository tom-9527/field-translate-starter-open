package com.example.translate.spi;

import java.util.Collection;
import java.util.Map;

/**
 * RPC 翻译 SPI。
 * <p>
 * 设计意图：对 RPC 技术完全无感知，但支持批量调用。
 * </p>
 */
public interface RpcTranslateClient {

    /**
     * 批量获取翻译结果。
     * <p>
     * 设计意图：强制批量访问，避免 N+1 网络调用。
     * 实现需自行处理超时/异常并返回部分结果。
     * </p>
     *
     * @param service 服务标识（来自注解）
     * @param method 方法名或操作名（来自注解）
     * @param codes 原始 code 集合
     * @param param 预留参数（来自注解）
     * @return code -> 翻译值 映射
     */
    Map<Object, Object> batchFetch(String service,
                                   String method,
                                   Collection<Object> codes,
                                   String param);
}
