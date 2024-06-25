package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.controller.response.SearchUserInfoRes;
import com.outstagram.outstagram.controller.response.UserInfoRes;
import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.service.UserElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/elasticsearch-users")
public class UserElasticController {
    private final UserElasticsearchService userElasticsearchService;

    /**
     * 닉네임으로 유저 검색
     */
    @GetMapping("/nicknames")
    public ResponseEntity<List<SearchUserInfoRes>> searchNicknameWithElastic(@RequestParam String search) {
        List<UserDocument> userDocumentList = userElasticsearchService.findByNickname(search);

        List<SearchUserInfoRes> response = userDocumentList.stream()
                .map(doc -> SearchUserInfoRes.builder()
                        .userId(doc.getId())
                        .nickname(doc.getNickname())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);

    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoRes> getUser(@PathVariable Long userId) {
        UserDocument user = userElasticsearchService.findById(userId);

        UserInfoRes response = UserInfoRes.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<UserDocument>> getAllUsers() {
        Iterable<UserDocument> all = userElasticsearchService.findAll();

        return ResponseEntity.ok(all);
    }

}