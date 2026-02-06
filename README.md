# Field Translate Starter

Base Maven project for a reusable field-translation module.

## Tech Stack
- Java 17
- Maven

## Fallback Strategy
- Translation failures never throw exceptions.
- When translation returns `null`, the framework uses the annotation `fallback` value if present.
- If `fallback` is empty, it uses the raw value so the response remains complete.

## Safety Notes
- Target display fields must be explicitly declared by `target` to prevent overwriting raw values.
- If the target field already has a non-null value, it will not be overwritten.
- Null raw values are ignored to avoid unnecessary lookups.
- Circular references are tracked by identity to prevent infinite recursion.

## Usage Example

VO definition:

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

JSON before translation:

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

JSON after translation:

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

## Integration Steps (3 or less)
1. Register `TranslateResponseBodyAdvice` and `TranslateExecutor` as Spring beans.
2. Register `TranslateHandler` implementations (ENUM/CACHE/TABLE/RPC) in your registry.
3. Provide optional SPI implementations (`DictCacheProvider`, `RpcTranslateClient`) for your environment.

## Extension Points
- `TranslateHandler`: add new translation types without touching core flow.
- `DictCacheProvider`: plug in local cache or Redis.
- `RpcTranslateClient`: integrate Feign, Dubbo, or other RPC stack.
- `TranslateHandlerRegistry`: custom registry or auto-discovery of handlers.
