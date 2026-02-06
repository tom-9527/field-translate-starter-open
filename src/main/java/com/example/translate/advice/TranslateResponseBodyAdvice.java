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
 * Triggers translation right before the response body is written.
 * <p>
 * Design intent: ensure translation happens after controller execution so
 * service and DAO layers remain unaware of any translation concerns.
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
        // Always applicable; translation decides at runtime via TranslateContext.
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // Respect global and request-level switches from the TranslateContext.
        if (!TranslateContext.current().isEnabled()) {
            return body;
        }
        return executor.translate(body);
    }
}
