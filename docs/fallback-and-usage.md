# Fallback and Integration Notes

## Unified Fallback Strategy
- All translation handlers are required to swallow exceptions and return safe results.
- When a translation fails, the framework will use:
  - `fallback` from `@TranslateField` if provided
  - otherwise, the raw value
- Missing handlers or missing metadata never break the response flow.

## Field Overwrite Protection
- Translation writes only to the explicit `target` field.
- If `target` already contains a non-null value, it is not overwritten.

## Null Safety
- Null raw values are skipped and never passed to handlers.
- Handler implementations are expected to handle nulls defensively.

## Recommended Cache Key Formats
- Dictionary cache: `dict:{dictKey}:{code}`
- Table translation: `table:{table}:{keyColumn}:{valueColumn}:{code}`

## Extension Points
- `TranslateHandler`: add new translation types.
- `TranslateHandlerRegistry`: control handler registration and routing.
- `DictCacheProvider`: connect to Redis or in-memory cache.
- `RpcTranslateClient`: connect to Feign/Dubbo/etc.

## Scope and Boundaries
- Table translation is intended for small/low-frequency lookup tables.
- If your table is large or highly dynamic, consider preloading into cache.
