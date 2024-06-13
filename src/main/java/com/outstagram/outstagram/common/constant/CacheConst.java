package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConst {

    public static final String POST = "postInfo";
    public static final String USER = "userInfo";
    public static final String COMMENT = "comments";
    public static final String IMAGE = "images";
    public static final int IN_CACHE = 2;
    public static final int IN_DB = 1;
    public static final int NOT_FOUND = 0;

}
