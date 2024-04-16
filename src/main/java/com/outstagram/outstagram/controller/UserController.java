package com.outstagram.outstagram.controller;

import static com.outstagram.outstagram.common.SessionConst.LOGIN_USER;

import com.outstagram.outstagram.controller.request.UserLoginReq;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("check-duplicated-email")
    public ResponseEntity<String> isDuplicatedEmail(@RequestParam String email) {
        userService.validateDuplicatedEmail(email);
        return ResponseEntity.ok().body("해당 이메일 사용 가능합니다.");
    }

    @GetMapping("check-duplicated-nickname")
    public ResponseEntity<String> isDuplicatedNickName(@RequestParam String nickname) {
        userService.validateDuplicatedNickname(nickname);
        return ResponseEntity.ok().body("해당 닉네임이 사용 가능합니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
        @RequestBody @Valid UserDTO userInfo
    ) {
        userService.insertUser(userInfo);
        return ResponseEntity.ok().body("회원가입 성공");
    }


    /**
     * 세션 로그인 처리
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody @Valid
            UserLoginReq userLoginReq,
            HttpServletRequest request
    ) {
        UserDTO user = userService.login(userLoginReq.getEmail(), userLoginReq.getPassword());
        log.info("==============loginUser = {}", user);

        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        // 로그인 성공 처리

        // 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(LOGIN_USER, user);

        return ResponseEntity.ok().body("로그인 성공");

    }




}
