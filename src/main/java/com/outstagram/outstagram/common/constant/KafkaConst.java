package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaConst {

    public static final String FEED_TOPIC = "feed";

    public static final String FEED_GROUPID = "sns-feed";

    public static final String DELETE_TOPIC = "post-delete";

    public static final String DELETE_GROUPID = "post";



}
