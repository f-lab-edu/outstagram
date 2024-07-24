package com.outstagram.outstagram.config.database;

public class DataSourceContextHolder {
    private static final ThreadLocal<Integer> contextHolder = new ThreadLocal<>();

    public static void setShardId(int shardId) {
        contextHolder.set(shardId);
    }

    public static Integer getShardId() {
        return contextHolder.get();
    }

    public static void clearShardId() {
        contextHolder.remove();
    }
}
