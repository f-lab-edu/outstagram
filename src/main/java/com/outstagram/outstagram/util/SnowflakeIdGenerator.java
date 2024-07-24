package com.outstagram.outstagram.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

@Component
@RequiredArgsConstructor
public class SnowflakeIdGenerator {
    private final Snowflake snowflake0;
    private final Snowflake snowflake1;
    
    public long snowflakeIdGenerator(long userId) {
        long nodeId = userId % DB_COUNT;
        long id;
        if (nodeId == 0) {
            id = snowflake0.nextId();
        } else {
            id = snowflake1.nextId();
        }
        return id;
    }
}
