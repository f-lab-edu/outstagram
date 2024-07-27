package com.outstagram.outstagram.config.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
public class DataSourceRouting extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            Long shardId = (Long) requestAttributes.getAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
            log.info("=============================== Current Shard ID: {}", shardId);
            return shardId;
        }
        log.warn("Request attributes are null, defaulting to shard 0");
        return 0L;
    }
}
