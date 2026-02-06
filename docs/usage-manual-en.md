# Usage Manual (English)

This manual explains how to integrate the field-translation module into another project, how to use it, and how to extend it.

## 1. Integration Options

### 1.1 Add as a Maven Dependency (Recommended)
Include this module as a dependency in your target project's `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>field-translate-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Notes:
- Replace `groupId` / `artifactId` / `version` with your actual coordinates.
- For multi-module builds, you can `mvn install` this module and reference it from other modules.

### 1.2 Copy Source Code (Not Recommended)
Only suitable for quick experiments. Maintenance and upgrades become manual.

## 2. Runtime Bean Registration

The module relies on three core components:
- `TranslateResponseBodyAdvice`: triggers translation right before the response body is written.
- `TranslateExecutor`: traverses the object graph and executes translation.
- `TranslateHandlerRegistry`: routes handlers by `TranslateType`.

Register them as Spring beans. Example:

```java
@Configuration
public class TranslateAutoConfig {

    @Bean
    public TranslateHandlerRegistry translateHandlerRegistry(List<TranslateHandler> handlers) {
        return type -> {
            for (TranslateHandler handler : handlers) {
                if (handler.type() == type) {
                    return handler;
                }
            }
            return null;
        };
    }

    @Bean
    public TranslateExecutor translateExecutor(TranslateHandlerRegistry registry) {
        return new DefaultTranslateExecutor(registry);
    }

    @Bean
    public TranslateResponseBodyAdvice translateResponseBodyAdvice(TranslateExecutor executor) {
        return new TranslateResponseBodyAdvice(executor);
    }
}
```

## 3. Basic Usage

Mark raw fields in your VO/DTO with `@TranslateField` and specify a `target` field:

```java
public class UserProfileVO {
    @TranslateField(type = TranslateType.ENUM, enumClass = UserStatus.class, target = "statusName")
    private Integer status;
    private String statusName;

    @TranslateField(type = TranslateType.CACHE, dictKey = "dept", target = "deptName")
    private String deptCode;
    private String deptName;
}
```

Rules:
- Raw values live in raw fields (e.g., `status`).
- Translated values are written into `target` fields (e.g., `statusName`).

## 4. Handlers and SPI Implementations

### 4.1 Enum Translation (ENUM)
Implement `CodeEnum` to expose `code -> desc` mapping:

```java
public enum UserStatus implements CodeEnum<Integer> {
    DISABLED(0, "Disabled"),
    ENABLED(1, "Enabled");
    // ...
}
```

### 4.2 Cache Dictionary Translation (CACHE)
Implement `DictCacheProvider` with batch fetching:

```java
@Component
public class RedisDictCacheProvider implements DictCacheProvider {
    @Override
    public Map<Object, Object> getBatch(String dictKey, Collection<Object> codes) {
        // Fetch from Redis or in-memory cache
        return Collections.emptyMap();
    }
}
```

### 4.3 Table Translation (TABLE)
Use `TableTranslateHandler` with `JdbcTemplate` for lightweight lookups.
Caching is recommended for performance.

### 4.4 RPC Translation (RPC)
Implement `RpcTranslateClient` to call external services in batch:

```java
@Component
public class CustomRpcTranslateClient implements RpcTranslateClient {
    @Override
    public Map<Object, Object> batchFetch(String service, String method,
                                          Collection<Object> codes, String param) {
        return Collections.emptyMap();
    }
}
```

## 5. Extensions

### 5.1 Add a New Translation Type
1. Add a new enum value in `TranslateType`.
2. Implement `TranslateHandler`.
3. Register your handler in `TranslateHandlerRegistry`.

### 5.2 Custom Registry
If you need conditional handler selection or multiple routing strategies, provide a custom `TranslateHandlerRegistry`.

### 5.3 Request-Level Switches
Use `TranslateContext` to control translation behavior per request:

```java
TranslateContext.current().setEnabled(false);
```

Or restrict allowed translation types:

```java
TranslateContext.current().setEnabledTypes(Set.of("ENUM", "CACHE"));
```

## 6. Notes
- `target` must be explicitly set; otherwise no write will happen.
- Existing non-null `target` values are not overwritten.
- Null raw values are skipped.
- Handlers should swallow exceptions to protect the main flow.
