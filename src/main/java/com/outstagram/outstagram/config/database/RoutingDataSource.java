package com.outstagram.outstagram.config.database;

import com.outstagram.outstagram.config.database.DatabaseContextHolder.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DatabaseType type = DatabaseContextHolder.getDatabaseType();
        log.info("========== DB 라우팅 : {}", type);
        return type;
    }
}
