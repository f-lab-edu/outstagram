package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.repository.UserElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserElasticsearchService {

    private final UserElasticsearchRepository userElasticsearchRepository;
    public void save(UserDocument user) {
        userElasticsearchRepository.save(user);
    }

    @Transactional
    public void edit(UserDocument user) {
        UserDocument findUser = userElasticsearchRepository.findById(user.getId()).orElse(null);

        if (findUser == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND_ESDB);
        }

        findUser.setNickname(user.getNickname());
        userElasticsearchRepository.save(findUser);
    }

    public List<UserDocument> findByNickname(String keyword) {
        return userElasticsearchRepository.findByNicknameContaining(keyword);
    }

}
