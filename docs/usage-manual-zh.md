# 使用手册（中文）

本手册说明如何在其他项目中引入字段翻译模块，并演示基础用法与扩展方式。

## 1. 引入方式

### 1.1 以模块依赖方式引入（推荐）
将该模块作为子模块或独立模块引入你的项目，并在父工程或应用的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>field-translate-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

说明：
- `groupId` / `artifactId` / `version` 请按你实际的发布坐标填写。
- 如果你采用多模块工程，可直接 `mvn install` 本模块后在目标项目中引用。

### 1.2 源码拷贝（不推荐）
仅适用于快速试验。请注意维护成本更高，升级需手动同步。

## 2. 运行时组件注册

本模块通过三个核心组件协作：
- `TranslateResponseBodyAdvice`：在响应体写出前触发翻译。
- `TranslateExecutor`：遍历对象图并执行翻译。
- `TranslateHandlerRegistry`：根据 `TranslateType` 路由处理器。

你需要将它们注册为 Spring Bean。示例：

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

## 3. 基础用法

在 VO/DTO 字段上使用 `@TranslateField` 标注原始字段，并指定展示字段 `target`：

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

翻译规则：
- 原始值写在原字段（如 `status`）。
- 翻译结果写入 `target` 字段（如 `statusName`）。

## 4. 处理器与SPI实现

### 4.1 枚举翻译（ENUM）
通过实现 `CodeEnum` 提供 `code -> desc` 映射：

```java
public enum UserStatus implements CodeEnum<Integer> {
    DISABLED(0, "Disabled"),
    ENABLED(1, "Enabled");
    // ...
}
```

### 4.2 缓存字典翻译（CACHE）
实现 `DictCacheProvider`，支持批量查询：

```java
@Component
public class RedisDictCacheProvider implements DictCacheProvider {
    @Override
    public Map<Object, Object> getBatch(String dictKey, Collection<Object> codes) {
        // 从 Redis/本地缓存批量取值
        return Collections.emptyMap();
    }
}
```

### 4.3 表翻译（TABLE）
使用 `TableTranslateHandler` 通过 `JdbcTemplate` 做轻量查询。
建议配合缓存减少数据库压力。

### 4.4 RPC 翻译（RPC）
实现 `RpcTranslateClient`，支持外部服务批量翻译：

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

## 5. 扩展方式

### 5.1 新增翻译类型
1. 在 `TranslateType` 中新增枚举值。
2. 实现 `TranslateHandler`。
3. 将处理器注册到 `TranslateHandlerRegistry`。

### 5.2 自定义注册表
如果你希望按条件启用处理器或引入多种策略，可自定义 `TranslateHandlerRegistry` 实现。

### 5.3 请求级开关
通过 `TranslateContext` 控制启用开关：

```java
TranslateContext.current().setEnabled(false);
```

或设置允许的翻译类型集合：

```java
TranslateContext.current().setEnabledTypes(Set.of("ENUM", "CACHE"));
```

## 6. 注意事项
- `target` 必须显式声明，否则不会写入结果。
- 若 `target` 字段已有非空值，则不会覆盖。
- 原始值为空时不会触发翻译。
- 处理器需自行吞掉异常，确保主流程不受影响。
