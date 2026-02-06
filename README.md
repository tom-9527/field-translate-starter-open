# Field Translate Starter / 字段翻译 Starter

可复用字段翻译模块的基础 Maven 工程。  
Base Maven project for a reusable field-translation module.

## 技术栈 / Tech Stack
- Java 17
- Maven

## 兜底策略 / Fallback Strategy
- 翻译失败不抛异常。Translation failures never throw exceptions.
- 当翻译返回 `null` 时，若注解配置了 `fallback` 则优先使用。When translation returns `null`, the framework uses the annotation `fallback` value if present.
- 若 `fallback` 为空，则使用原始值，确保响应完整。If `fallback` is empty, it uses the raw value so the response remains complete.

## 安全说明 / Safety Notes
- 必须通过 `target` 明确声明展示字段，避免覆盖原始值。Target display fields must be explicitly declared by `target` to prevent overwriting raw values.
- 若目标字段已有非空值，则不会被覆盖。If the target field already has a non-null value, it will not be overwritten.
- 原始值为 `null` 时将被跳过，避免无意义查询。Null raw values are ignored to avoid unnecessary lookups.
- 通过对象身份跟踪循环引用，防止无限递归。Circular references are tracked by identity to prevent infinite recursion.

## 使用示例 / Usage Example

VO 定义 / VO definition:

```java
public class UserProfileVO {
    @TranslateField(type = TranslateType.ENUM, enumClass = UserStatus.class, target = "statusName")
    private Integer status;
    private String statusName;

    @TranslateField(type = TranslateType.CACHE, dictKey = "dept", target = "deptName")
    private String deptCode;
    private String deptName;

    @TranslateField(type = TranslateType.TABLE, table = "org", keyColumn = "id", valueColumn = "name", target = "orgName")
    private Long orgId;
    private String orgName;

    @TranslateField(type = TranslateType.RPC, rpcService = "user-profile", rpcMethod = "batchName", target = "remoteName")
    private String remoteCode;
    private String remoteName;
}
```

翻译前 JSON / JSON before translation:

```json
{
  "status": 1,
  "statusName": null,
  "deptCode": "d01",
  "deptName": null,
  "orgId": 1001,
  "orgName": null,
  "remoteCode": "r9",
  "remoteName": null
}
```

翻译后 JSON / JSON after translation:

```json
{
  "status": 1,
  "statusName": "Enabled",
  "deptCode": "d01",
  "deptName": "R&D",
  "orgId": 1001,
  "orgName": "HQ",
  "remoteCode": "r9",
  "remoteName": "RemoteName"
}
```

## 接入步骤（<=3步） / Integration Steps (3 or less)
1. 注册 `TranslateResponseBodyAdvice` 与 `TranslateExecutor` 为 Spring Bean。Register `TranslateResponseBodyAdvice` and `TranslateExecutor` as Spring beans.
2. 在注册表中注册 `TranslateHandler` 实现（ENUM/CACHE/TABLE/RPC）。Register `TranslateHandler` implementations (ENUM/CACHE/TABLE/RPC) in your registry.
3. 提供可选 SPI 实现（`DictCacheProvider`、`RpcTranslateClient`）以适配环境。Provide optional SPI implementations (`DictCacheProvider`, `RpcTranslateClient`) for your environment.

## 扩展点 / Extension Points
- `TranslateHandler`：新增翻译类型而不修改核心流程。Add new translation types without touching core flow.
- `DictCacheProvider`：接入本地缓存或 Redis。Plug in local cache or Redis.
- `RpcTranslateClient`：接入 Feign、Dubbo 等 RPC 栈。Integrate Feign, Dubbo, or other RPC stack.
- `TranslateHandlerRegistry`：自定义注册表或自动发现处理器。Custom registry or auto-discovery of handlers.
