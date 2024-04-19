package com.outstagram.outstagram.service;

import static com.outstagram.outstagram.util.SHA256Util.encryptedPassword;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.UserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 유저 회원가입 메서드 비밀번호는 sha256으로 암호화해 저장
     */

    public void insertUser(UserDTO userInfo) {
        
        // 이메일, 닉네임 중 중복되는게 있을 때
        if (!validateUserInfo(userInfo)) {
            throw new ApiException(ErrorCode.DUPLICATED);
        }

        userInfo.setCreateDate(LocalDateTime.now());
        userInfo.setUpdateDate(LocalDateTime.now());

        userInfo.setPassword(encryptedPassword(userInfo.getPassword()));

        int insertCount = userMapper.insertUser(userInfo);

        if (insertCount != 1) {
            log.error("insert user ERROR!!! {}", userInfo);
            throw new ApiException(ErrorCode.INSERT_ERROR);
        }
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
     * email, nickname 둘 다 중복되지 않을 경우 -> true
     */
    private boolean validateUserInfo(UserDTO userInfo) {
        return !validateDuplicatedEmail(userInfo.getEmail()) && !validateDuplicatedNickname(
            userInfo.getNickname());
    }

    /**
     * 중복 -> true
     */
    public boolean validateDuplicatedEmail(String email) {
        int count = userMapper.countByEmail(email);
        return count > 0;
    }

    public boolean validateDuplicatedNickname(String nickname) {
        int count = userMapper.countByNickname(nickname);
        return count > 0;
    }

}
