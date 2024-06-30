package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaConst {

    public static final String FEED_TOPIC = "feed";

    public static final String FEED_GROUPID = "sns-feed";

    public static final String DELETE_TOPIC = "post-delete";

    public static final String DELETE_GROUPID = "post";

    public static final String SEND_NOTIFICATION = "send-notification";
    public static final String NOTIFICATION_GROUPID = "notification";
    public static final String USER_UPSERT_TOPIC = "user-upsert";
    public static final String USER_DELETE_TOPIC = "user-delete";
    public static final String USER_GROUPID = "user";

    public static final String POST_SAVE_TOPIC = "post-save";
    public static final String POST_EDIT_TOPIC = "post-edit";
    public static final String POST_GROUPID = "post";



}
