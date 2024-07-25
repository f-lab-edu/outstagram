package com.outstagram.outstagram.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DataSourceRouting extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        Long shardId = DataSourceContextHolder.getShardId();
        log.info("=============================== Current Shard ID: {}", shardId);
        return shardId;
    }
}
