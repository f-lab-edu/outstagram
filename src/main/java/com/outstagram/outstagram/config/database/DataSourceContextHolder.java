package com.outstagram.outstagram.config.database;

public class DataSourceContextHolder {
    private static final ThreadLocal<Long> contextHolder = new ThreadLocal<>();

    public static void setShardId(Long shardId) {
        contextHolder.set(shardId);
    }

    public static Long getShardId() {
        return contextHolder.get();
    }

    public static void clearShardId() {
        contextHolder.remove();
    }
}
