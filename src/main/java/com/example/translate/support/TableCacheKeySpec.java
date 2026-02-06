package com.example.translate.support;

/**
 * 表字段翻译缓存 key 规范。
 * <p>
 * 设计意图：明确 key 的命名空间，以避免不同表/列之间冲突。
 * </p>
 */
public final class TableCacheKeySpec {

    /**
     * 表翻译前缀。
     * <p>
     * 命名空间格式：table:{table}:{keyColumn}:{valueColumn}
     * 示例：table:sys_user:id:name:1001 -> "Alice"
     * </p>
     */
    public static final String TABLE_PREFIX = "table";

    private TableCacheKeySpec() {
    }

    /**
     * 构建表翻译缓存命名空间。
     * <p>
     * 设计意图：以表名与列名分组缓存，避免数据串用。
     * </p>
     *
     * @param table 表名
     * @param keyColumn 键列
     * @param valueColumn 值列
     * @return 命名空间
     */
    public static String namespace(String table, String keyColumn, String valueColumn) {
        return TABLE_PREFIX + ":" + table + ":" + keyColumn + ":" + valueColumn;
    }
}
