package com.outstagram.outstagram.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptimisticLockConst {
    // Version 충돌 시, 재시도 횟수
    public static final int MAX_RETRIES = 10;

}
