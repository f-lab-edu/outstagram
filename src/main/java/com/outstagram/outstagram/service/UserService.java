package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.util.SHA256Util.encryptedPassword;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.UserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 유저 회원가입 메서드 비밀번호는 sha256으로 암호화해 저장
     */
    public void insertUser(UserDTO userInfo) {
        
        // 이메일, 닉네임 중 중복 체크
        validateUserInfo(userInfo);

        userInfo.setCreateDate(LocalDateTime.now());
        userInfo.setUpdateDate(LocalDateTime.now());

        userInfo.setPassword(encryptedPassword(userInfo.getPassword()));

        userMapper.insertUser(userInfo);
        saveUserToRedis(userInfo);
    }


    /**
     * 로그인 메서드
     */
    public UserDTO login(String email, String password) {
        String cryptoPassword = encryptedPassword(password);
        return userMapper.findByEmailAndPassword(email, cryptoPassword);
    }

    //==validator method==//



    /**
     * 중복 -> true
     */
    public void validateDuplicatedEmail(String email) {
        int count = userMapper.countByEmail(email);
        if (count > 0) {
            throw new ApiException(ErrorCode.DUPLICATED);
        }
    }

    public void validateDuplicatedNickname(String nickname) {
        int count = userMapper.countByNickname(nickname);
        if (count > 0) {
            throw new ApiException(ErrorCode.DUPLICATED);
        }
    }

    /**
     * userId로 유저 찾기
     */
    public UserDTO findByUserId(Long userId) {
        return userMapper.findById(userId);
    }





    /* ========================================================================================== */

    /**
     * email, nickname 둘 다 중복되지 않을 경우 -> true
     */
    private void validateUserInfo(UserDTO userInfo) {
        validateDuplicatedEmail(userInfo.getEmail());
        validateDuplicatedNickname(userInfo.getNickname());
    }

    /**
     * redis에 유저 정보 캐싱해놓기
     */
    private void saveUserToRedis(UserDTO userInfo) {
        // Redis의 Hash 구조를 사용하여 유저 정보 저장
        String userKey = "user:" + userInfo.getId();  // Redis에서 사용할 키
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(userKey, "nickname", userInfo.getNickname());
        hashOps.put(userKey, "profileImage", userInfo.getImgUrl());
    }

}
