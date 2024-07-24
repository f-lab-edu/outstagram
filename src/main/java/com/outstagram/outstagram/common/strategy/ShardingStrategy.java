package com.outstagram.outstagram.common.strategy;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

public class ShardingStrategy {
    public static int getShardId(long userId) {
        return (int) (userId % DB_COUNT);
    }
}
