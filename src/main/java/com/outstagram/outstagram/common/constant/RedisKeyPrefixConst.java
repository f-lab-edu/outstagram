package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisKeyPrefixConst {
    public static final String LIKE_COUNT_PREFIX = "likeCount:";

    public static final String USER_LIKE_PREFIX = "userLike:";
    public static final String USER_UNLIKE_PREFIX = "userUnlike:";

    public static final String FOLLOWING = "followings:";

    public static final String FOLLOWER = "followers:";

    public static final String FEED = "feed:";

}
