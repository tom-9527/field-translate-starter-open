package com.example.translate.support;

/**
 * Cache key conventions for table-based translation.
 * <p>
 * Design intent: make key formats explicit and consistent across cache
 * implementations, while keeping table translation independent of a specific
 * cache technology.
 * </p>
 */
public final class TableCacheKeySpec {

    /**
     * Prefix for table-translation entries.
     * <p>
     * Format namespace: table:{table}:{keyColumn}:{valueColumn}
     * Entry key: {namespace}:{code}
     * Example: table:sys_user:id:name:1001 -> "Alice"
     * </p>
     */
    public static final String TABLE_PREFIX = "table";

    private TableCacheKeySpec() {
    }

    /**
     * Builds a cache namespace for a table translation set.
     * <p>
     * Design intent: allow caches to group entries by table/column pair so
     * lookups remain efficient and avoid collisions.
     * </p>
     *
     * @param table table name
     * @param keyColumn code column
     * @param valueColumn display column
     * @return namespace string
     */
    public static String namespace(String table, String keyColumn, String valueColumn) {
        return TABLE_PREFIX + ":" + table + ":" + keyColumn + ":" + valueColumn;
    }
}
