package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.controller.request.UserLoginReq;
import com.outstagram.outstagram.controller.response.UserLoginRes;
import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.UserErrorCode;
import com.outstagram.outstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.outstagram.outstagram.common.SessionConst.LOGIN_USER;
import static com.outstagram.outstagram.controller.response.UserLoginRes.LoginStatus.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("check-duplicated-email")
    public ResponseEntity<String> checkDuplicatedEmail(@RequestParam String email) {
        boolean isDuplicate = userService.validateDuplicatedEmail(email);
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일이 중복됩니다.");
        } else {
            return ResponseEntity.ok().body("해당 이메일 사용 가능합니다.");
        }
    }

    @GetMapping("check-duplicated-nickname")
    public ResponseEntity<String> checkDuplicatedNickName(@RequestParam String nickname) {
        boolean isDuplicate = userService.validateDuplicatedNickname(nickname);
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
//        // 이메일, 비밀번호, 닉네임 중 하나라도 null이 있을 경우
//        if (!UserDTO.checkSignupData(userInfo)) {
//            throw new NullPointerException("이메일, 비밀번호, 닉네임 모두 입력해야 합니다.");
//        }
        userService.insertUser(userInfo);
    }



    /**
     * 세션 로그인 처리
     * @param userLoginReq
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginRes> login(
            @RequestBody @Valid
            UserLoginReq userLoginReq,
            HttpServletRequest request
    ) {
        UserDTO user = userService.login(userLoginReq.getEmail(), userLoginReq.getPassword());
        log.info("==============loginUser = {}", user);

        if (user == null) {
            throw new ApiException(UserErrorCode.USER_NOT_FOUND);
        }

        // 로그인 성공 처리

        // 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(LOGIN_USER, user);

        return ResponseEntity
                .ok(
                        UserLoginRes.builder()
                        .result(SUCCESS)
                        .build()
                );

    }




}
