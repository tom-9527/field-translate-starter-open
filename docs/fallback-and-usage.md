# 兜底与接入说明 / Fallback and Integration Notes

## 统一兜底策略 / Unified Fallback Strategy
- 所有翻译处理器必须吞掉异常并返回安全结果。All translation handlers are required to swallow exceptions and return safe results.
- 当翻译失败时，优先使用 `@TranslateField` 的 `fallback` 值，否则使用原始值。When a translation fails, the framework uses `fallback` if provided, otherwise the raw value.
- 缺失处理器或缺失元信息不会中断响应流程。Missing handlers or missing metadata never break the response flow.

## 字段覆盖保护 / Field Overwrite Protection
- 翻译只写入明确声明的 `target` 字段。Translation writes only to the explicit `target` field.
- 若 `target` 已有非空值，不会被覆盖。If `target` already contains a non-null value, it is not overwritten.

## 空值安全 / Null Safety
- 原始值为 `null` 时将被跳过，不会交给处理器。Null raw values are skipped and never passed to handlers.
- 处理器实现应对 `null` 做防御式处理。Handler implementations are expected to handle nulls defensively.

## 推荐缓存 Key 格式 / Recommended Cache Key Formats
- 字典缓存：`dict:{dictKey}:{code}`. Dictionary cache: `dict:{dictKey}:{code}`.
- 表翻译：`table:{table}:{keyColumn}:{valueColumn}:{code}`. Table translation: `table:{table}:{keyColumn}:{valueColumn}:{code}`.

## 扩展点 / Extension Points
- `TranslateHandler`：新增翻译类型。Add new translation types.
- `TranslateHandlerRegistry`：控制处理器注册与路由。Control handler registration and routing.
- `DictCacheProvider`：接入 Redis 或内存缓存。Connect to Redis or in-memory cache.
- `RpcTranslateClient`：接入 Feign/Dubbo 等。Connect to Feign/Dubbo/etc.

## 范围与边界 / Scope and Boundaries
- 表翻译适用于小表或低频查询。Table translation is intended for small/low-frequency lookup tables.
- 若表很大或变化频繁，建议预加载到缓存。If your table is large or highly dynamic, consider preloading into cache.
