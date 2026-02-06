package com.example.translate.handler.impl;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;
import com.example.translate.context.TranslateContext;
import com.example.translate.handler.TranslateHandler;
import com.example.translate.spi.DictCacheProvider;
import com.example.translate.support.TableCacheKeySpec;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Default handler for table-based translation using a lightweight lookup.
 * <p>
 * Design intent: translate foreign-key-like values without forcing JOINs or
 * ORM mappings in business SQL. The handler batches lookups to avoid N+1 and
 * degrades safely on any error.
 * </p>
 */
public class TableTranslateHandler implements TranslateHandler {

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern TABLE_NAME = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$");

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final DictCacheProvider cacheProvider;

    public TableTranslateHandler(JdbcTemplate jdbcTemplate, DictCacheProvider cacheProvider) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheProvider = cacheProvider;
    }

    @Override
    public TranslateType type() {
        return TranslateType.TABLE;
    }

    @Override
    public Map<Object, Object> batchTranslate(Collection<Object> rawValues,
                                              TranslateField meta,
                                              TranslateContext context) {
        if (rawValues == null || rawValues.isEmpty()) {
            return Collections.emptyMap();
        }

        String table = meta.table();
        String keyColumn = meta.keyColumn();
        String valueColumn = meta.valueColumn();
        if (!isValidIdentifier(table, keyColumn, valueColumn)) {
            // Reject unsafe identifiers to avoid SQL injection risks.
            return Collections.emptyMap();
        }

        Set<Object> uniqueValues = new HashSet<>();
        for (Object rawValue : rawValues) {
            if (rawValue != null) {
                uniqueValues.add(rawValue);
            }
        }
        if (uniqueValues.isEmpty()) {
            return Collections.emptyMap();
        }

        // Step 1: attempt cache lookup to reduce database access.
        Map<Object, Object> result = new HashMap<>();
        Set<Object> pending = new HashSet<>(uniqueValues);
        if (cacheProvider != null) {
            String namespace = TableCacheKeySpec.namespace(table, keyColumn, valueColumn);
            Map<Object, Object> cached = safeCacheBatch(namespace, pending);
            if (cached != null && !cached.isEmpty()) {
                result.putAll(cached);
                pending.removeAll(cached.keySet());
            }
        }

        // Step 2: if cache misses remain, query the database in batches.
        if (!pending.isEmpty() && jdbcTemplate != null) {
            List<Object> pendingList = new ArrayList<>(pending);
            for (int i = 0; i < pendingList.size(); i += DEFAULT_BATCH_SIZE) {
                int end = Math.min(i + DEFAULT_BATCH_SIZE, pendingList.size());
                List<Object> batch = pendingList.subList(i, end);
                Map<Object, Object> dbResult = safeQuery(table, keyColumn, valueColumn, batch);
                if (dbResult != null && !dbResult.isEmpty()) {
                    result.putAll(dbResult);
                }
            }
        }

        return result;
    }

    private Map<Object, Object> safeCacheBatch(String namespace, Collection<Object> codes) {
        try {
            // Cache provider chooses the storage backend; miss should not throw.
            return cacheProvider.getBatch(namespace, codes);
        } catch (RuntimeException ex) {
            return Collections.emptyMap();
        }
    }

    private Map<Object, Object> safeQuery(String table,
                                          String keyColumn,
                                          String valueColumn,
                                          List<Object> batch) {
        try {
            String sql = buildSql(table, keyColumn, valueColumn, batch.size());
            return jdbcTemplate.query(sql, batch.toArray(), rs -> readResult(rs, keyColumn, valueColumn));
        } catch (RuntimeException ex) {
            // Query failures must not break the main flow.
            return Collections.emptyMap();
        }
    }

    private String buildSql(String table, String keyColumn, String valueColumn, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ").append(keyColumn).append(", ").append(valueColumn)
                .append(" from ").append(table)
                .append(" where ").append(keyColumn).append(" in (");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    private Map<Object, Object> readResult(ResultSet rs, String keyColumn, String valueColumn) throws Exception {
        Map<Object, Object> map = new HashMap<>();
        while (rs.next()) {
            Object key = rs.getObject(keyColumn);
            Object value = rs.getObject(valueColumn);
            if (key != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    private boolean isValidIdentifier(String table, String keyColumn, String valueColumn) {
        if (table == null || keyColumn == null || valueColumn == null) {
            return false;
        }
        if (table.isEmpty() || keyColumn.isEmpty() || valueColumn.isEmpty()) {
            return false;
        }
        // Only allow safe characters to avoid SQL injection via annotation values.
        return TABLE_NAME.matcher(table).matches()
                && IDENTIFIER.matcher(keyColumn).matches()
                && IDENTIFIER.matcher(valueColumn).matches();
    }
}
