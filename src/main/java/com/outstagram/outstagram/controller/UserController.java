package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        Boolean isDuplicate = userService.isDuplicatedEmail(email);
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일이 중복됩니다.");
        } else {
            return ResponseEntity.ok().body("해당 이메일 사용 가능합니다.");
        }
    }

    @GetMapping("check-nickname")
    public ResponseEntity<String> checkNickName(@RequestParam String nickname) {
        Boolean isDuplicate = userService.isDuplicatedEmail(nickname);
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("닉네임이 중복됩니다.");
        } else {
            return ResponseEntity.ok().body("해당 닉네임이 사용 가능합니다.");
        }
    }

    @PostMapping("/signup")
    public void signup(
            @RequestBody @Valid UserDTO userInfo
    ) {
        // 이메일, 비밀번호, 닉네임 중 하나라도 null이 있을 경우
        if (!UserDTO.checkSignupData(userInfo)) {    
            throw new NullPointerException("이메일, 비밀번호, 닉네임 모두 입력해야 합니다.");
        }
        userService.insertUser(userInfo);
    }
}


