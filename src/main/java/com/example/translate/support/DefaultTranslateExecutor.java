package com.example.translate.support;

import com.example.translate.annotation.TranslateField;
import com.example.translate.context.TranslateContext;
import com.example.translate.handler.TranslateHandler;
import com.example.translate.registry.TranslateHandlerRegistry;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default translation executor that walks the response object graph and applies
 * field translations based on {@link TranslateField} metadata.
 * <p>
 * Design intent: keep all traversal and safety rules in one place so the
 * ResponseBodyAdvice can remain minimal and focused on timing.
 * </p>
 */
public class DefaultTranslateExecutor implements TranslateExecutor {

    private final TranslateHandlerRegistry registry;

    // Cache reflective field lookups to reduce repeated scanning cost.
    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    public DefaultTranslateExecutor(TranslateHandlerRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object translate(Object body) {
        if (body == null) {
            return null;
        }

        TranslateContext context = TranslateContext.current();
        if (!context.isEnabled()) {
            // Respect global/request-level switches.
            return body;
        }

        TraversalState state = new TraversalState();
        processObject(body, context, state);
        return body;
    }

    private void processObject(Object value, TranslateContext context, TraversalState state) {
        if (value == null) {
            return;
        }

        Class<?> type = value.getClass();
        if (isSimpleValueType(type)) {
            // Primitive/immutable types do not contain translatable fields.
            return;
        }

        if (state.isVisited(value)) {
            // Prevent infinite loops on circular references.
            return;
        }
        state.markVisited(value);

        if (value instanceof Collection) {
            processCollection((Collection<?>) value, context, state);
            return;
        }

        if (value instanceof Map) {
            processMap((Map<?, ?>) value, context, state);
            return;
        }

        if (type.isArray()) {
            processArray(value, context, state);
            return;
        }

        if (isSpringPage(value)) {
            processSpringPage(value, context, state);
            return;
        }

        processPojo(value, context, state);
    }

    private void processCollection(Collection<?> collection, TranslateContext context, TraversalState state) {
        if (collection.isEmpty()) {
            return;
        }

        // Batch translation across a collection to avoid N+1 lookups.
        batchTranslateCollection(collection, context, state);

        // Recursively process nested objects after translation.
        for (Object element : collection) {
            processObject(element, context, state);
        }
    }

    private void processMap(Map<?, ?> map, TranslateContext context, TraversalState state) {
        if (map.isEmpty()) {
            return;
        }
        // Only values are traversed to avoid unexpected key mutation.
        for (Object value : map.values()) {
            processObject(value, context, state);
        }
    }

    private void processArray(Object array, TranslateContext context, TraversalState state) {
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            processObject(Array.get(array, i), context, state);
        }
    }

    private void processPojo(Object bean, TranslateContext context, TraversalState state) {
        for (Field field : getAllFields(bean.getClass())) {
            TranslateField meta = field.getAnnotation(TranslateField.class);
            if (meta == null) {
                // Not a translatable field, but still traverse nested objects.
                Object nested = readField(field, bean);
                processObject(nested, context, state);
                continue;
            }

            if (!context.isTypeEnabled(meta.type().name())) {
                // Skip types that are not enabled for this request.
                continue;
            }

            if (state.isFieldTranslated(bean, field.getName())) {
                // Avoid duplicate translation for the same field.
                continue;
            }

            String targetName = meta.target();
            if (targetName == null || targetName.isEmpty()) {
                // Explicit target is required to avoid overwriting raw values.
                continue;
            }

            Field targetField = findField(bean.getClass(), targetName);
            if (targetField == null) {
                // Target field does not exist; skip safely.
                continue;
            }

            Object rawValue = readField(field, bean);
            if (rawValue == null) {
                // Nothing to translate.
                continue;
            }

            TranslateHandler handler = registry.getHandler(meta.type());
            if (handler == null) {
                // No handler registered for this type; degrade gracefully.
                continue;
            }

            Object translated = safeTranslate(handler, rawValue, meta, context);
            writeTargetField(bean, targetField, rawValue, translated, meta, state);
        }
    }

    private void batchTranslateCollection(Collection<?> collection, TranslateContext context, TraversalState state) {
        Map<BatchKey, List<TaskItem>> tasks = new HashMap<>();

        for (Object element : collection) {
            if (element == null || isSimpleValueType(element.getClass())) {
                continue;
            }

            for (Field field : getAllFields(element.getClass())) {
                TranslateField meta = field.getAnnotation(TranslateField.class);
                if (meta == null) {
                    continue;
                }

                if (!context.isTypeEnabled(meta.type().name())) {
                    continue;
                }

                if (state.isFieldTranslated(element, field.getName())) {
                    continue;
                }

                String targetName = meta.target();
                if (targetName == null || targetName.isEmpty()) {
                    continue;
                }

                Field targetField = findField(element.getClass(), targetName);
                if (targetField == null) {
                    continue;
                }

                Object rawValue = readField(field, element);
                if (rawValue == null) {
                    continue;
                }

                TranslateHandler handler = registry.getHandler(meta.type());
                if (handler == null) {
                    continue;
                }

                BatchKey key = new BatchKey(handler, meta, targetField.getName());
                tasks.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new TaskItem(element, rawValue, targetField));
            }
        }

        for (Map.Entry<BatchKey, List<TaskItem>> entry : tasks.entrySet()) {
            BatchKey key = entry.getKey();
            List<TaskItem> items = entry.getValue();

            Collection<Object> rawValues = new ArrayList<>(items.size());
            for (TaskItem item : items) {
                rawValues.add(item.rawValue);
            }

            Map<Object, Object> translated = safeBatchTranslate(key.handler, rawValues, key.meta, context);
            for (TaskItem item : items) {
                Object mapped = translated.get(item.rawValue);
                writeTargetField(item.owner, item.targetField, item.rawValue, mapped, key.meta, state);
            }
        }
    }

    private Object safeTranslate(TranslateHandler handler, Object rawValue, TranslateField meta, TranslateContext context) {
        try {
            return handler.translate(rawValue, meta, context);
        } catch (RuntimeException ex) {
            // Translation must never break the main flow; swallow and degrade.
            return null;
        }
    }

    private Map<Object, Object> safeBatchTranslate(TranslateHandler handler,
                                                   Collection<Object> rawValues,
                                                   TranslateField meta,
                                                   TranslateContext context) {
        try {
            Map<Object, Object> result = handler.batchTranslate(rawValues, meta, context);
            return result == null ? Collections.emptyMap() : result;
        } catch (RuntimeException ex) {
            // Translation must never break the main flow; swallow and degrade.
            return Collections.emptyMap();
        }
    }

    private void writeTargetField(Object owner,
                                  Field targetField,
                                  Object rawValue,
                                  Object translated,
                                  TranslateField meta,
                                  TraversalState state) {
        Object finalValue = translated;
        if (finalValue == null) {
            // Use fallback or raw value if translation fails.
            String fallback = meta.fallback();
            finalValue = (fallback == null || fallback.isEmpty()) ? rawValue : fallback;
        }

        Object existing = readField(targetField, owner);
        if (existing != null) {
            // Do not override an existing target value to avoid field collisions.
            state.markFieldTranslated(owner, targetField.getName());
            return;
        }

        writeField(targetField, owner, finalValue);
        state.markFieldTranslated(owner, targetField.getName());
    }

    private List<Field> getAllFields(Class<?> type) {
        return fieldCache.computeIfAbsent(type, clazz -> {
            List<Field> fields = new ArrayList<>();
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                Collections.addAll(fields, current.getDeclaredFields());
                current = current.getSuperclass();
            }
            return fields;
        });
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Try superclass.
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private Object readField(Field field, Object owner) {
        try {
            if (!field.canAccess(owner)) {
                field.setAccessible(true);
            }
            return field.get(owner);
        } catch (IllegalAccessException | IncompatibleClassChangeError ex) {
            return null;
        }
    }

    private void writeField(Field field, Object owner, Object value) {
        try {
            if (!field.canAccess(owner)) {
                field.setAccessible(true);
            }
            field.set(owner, value);
        } catch (IllegalAccessException | IncompatibleClassChangeError ex) {
            // Ignore to keep translation non-intrusive.
        }
    }

    private boolean isSimpleValueType(Class<?> type) {
        return type.isPrimitive()
                || String.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type)
                || Character.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || UUID.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type)
                || type.getName().startsWith("java.time.")
                || type.getName().startsWith("java.lang.");
    }

    private static volatile Class<?> springPageClass;

    private boolean isSpringPage(Object value) {
        Class<?> pageClass = springPageClass;
        if (pageClass == null) {
            try {
                pageClass = Class.forName("org.springframework.data.domain.Page");
                springPageClass = pageClass;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }
        return pageClass.isInstance(value);
    }

    private void processSpringPage(Object page, TranslateContext context, TraversalState state) {
        try {
            Method getContent = page.getClass().getMethod("getContent");
            Object content = getContent.invoke(page);
            if (content instanceof Collection) {
                processCollection((Collection<?>) content, context, state);
            }
        } catch (ReflectiveOperationException ex) {
            // Ignore if Page signature is unexpected.
        }
    }

    private static final class TraversalState {
        private final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Object, Set<String>> translated = new IdentityHashMap<>();

        boolean isVisited(Object obj) {
            return visited.contains(obj);
        }

        void markVisited(Object obj) {
            visited.add(obj);
        }

        boolean isFieldTranslated(Object obj, String fieldName) {
            Set<String> fields = translated.get(obj);
            return fields != null && fields.contains(fieldName);
        }

        void markFieldTranslated(Object obj, String fieldName) {
            translated.computeIfAbsent(obj, k -> new HashSet<>()).add(fieldName);
        }
    }

    private static final class BatchKey {
        private final TranslateHandler handler;
        private final TranslateField meta;
        private final String targetName;

        BatchKey(TranslateHandler handler, TranslateField meta, String targetName) {
            this.handler = handler;
            this.meta = meta;
            this.targetName = targetName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BatchKey)) {
                return false;
            }
            BatchKey that = (BatchKey) o;
            return Objects.equals(handler, that.handler)
                    && Objects.equals(meta, that.meta)
                    && Objects.equals(targetName, that.targetName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(handler, meta, targetName);
        }
    }

    private static final class TaskItem {
        private final Object owner;
        private final Object rawValue;
        private final Field targetField;

        TaskItem(Object owner, Object rawValue, Field targetField) {
            this.owner = owner;
            this.rawValue = rawValue;
            this.targetField = targetField;
        }
    }
}
