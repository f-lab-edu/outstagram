package com.outstagram.outstagram.config.database;

public class DatabaseContextHolder {
    private static final ThreadLocal<DatabaseType> contextHolder = new ThreadLocal<>();

    public enum DatabaseType {
        MASTER, SLAVE
    }

    public static void setDatabaseType(DatabaseType type) {
        contextHolder.set(type);
    }

    // 현재 설정된 데이터베이스 타입을 반환합니다. 설정되어 있지 않으면 기본값인 MASTER를 반환합니다.
    public static DatabaseType getDatabaseType() {
        return contextHolder.get() != null ? contextHolder.get() : DatabaseType.MASTER;
    }

    public static void clearDatabaseType() {
        contextHolder.remove();
    }

}
