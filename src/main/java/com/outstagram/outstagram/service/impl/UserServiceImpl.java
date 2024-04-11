package com.outstagram.outstagram.service.impl;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.error.DuplicateEmailException;
import com.outstagram.outstagram.error.DuplicateNicknameException;
import com.outstagram.outstagram.mapper.UserMapper;
import com.outstagram.outstagram.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.outstagram.outstagram.util.SHA256Util.encryptSHA256;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 유저 회원가입 메서드
     * 비밀번호는 sha256으로 암호화해 저장
     */
    @Override
    public void insertUser(UserDTO userInfo) {
        if (isDuplicatedEmail(userInfo.getEmail())) throw new DuplicateEmailException("중복된 이메일 입니다.");
        if (isDuplicatedNickname(userInfo.getNickname())) throw new DuplicateNicknameException("중복된 닉네임 입니다.");

        userInfo.setCreateDate(LocalDateTime.now());
        userInfo.setUpdateDate(LocalDateTime.now());

        userInfo.setPassword(encryptSHA256(userInfo.getPassword()));

        int insertCount = userMapper.insertUser(userInfo);

        if (insertCount != 1) {
            log.error("insert user ERROR!!! {}", userInfo);
            throw new RuntimeException(
                    "insert user error!!! insertUser() 확인하기\n"
                    + "Params : " + userInfo
            );
        }
    }

    /**
     * email로 db 조회해서 1개 이상 나오면 중복, 0개 나오면 중복 X
     */
    @Override
    public Boolean isDuplicatedEmail(String email) {
        int count = userMapper.countByEmail(email);
        return count > 0;
    }

    @Override
    public Boolean isDuplicatedNickname(String nickname) {
        int count = userMapper.countByNickname(nickname);
        return count > 0;
    }
}
