package com.example.translate.advice;

import com.example.translate.context.TranslateContext;
import com.example.translate.support.TranslateExecutor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 在响应体写出前触发翻译。
 * <p>
 * 设计意图：确保翻译发生在 Controller 执行之后，
 * 使 Service/DAO 层无需感知翻译逻辑。
 * </p>
 */
@RestControllerAdvice
public class TranslateResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final TranslateExecutor executor;

    public TranslateResponseBodyAdvice(TranslateExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // 始终可用；是否翻译由 TranslateContext 在运行时决定。
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 遵循 TranslateContext 的全局与请求级开关。
        if (!TranslateContext.current().isEnabled()) {
            return body;
        }
        return executor.translate(body);
    }
}
