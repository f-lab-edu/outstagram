package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisKeyPrefixConst {

    public static final String LIKE_COUNT_PREFIX = "likeCount:";

    public static final String USER_LIKE_PREFIX = "userLike:";

    public static final String USER_BOOKMARK_PREFIX = "userBookmark:";

    public static final String FOLLOWING = "followings:";

    public static final String FOLLOWER = "followers:";

    public static final String FEED = "feed:";

    public static final String INSERT_USERLIKE_LOCK = "insert_userLike_lock";

    public static final String INSERT_BOOKMARK_LOCK = "insert_bookmark_lock";

}