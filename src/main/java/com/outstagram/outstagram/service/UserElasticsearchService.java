package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.repository.UserElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserElasticsearchService {

    private final UserElasticsearchRepository userElasticsearchRepository;

    public void save(UserDocument user) {
        userElasticsearchRepository.save(user);
    }

    public List<UserDocument> findByNickname(String searchText) {
        return userElasticsearchRepository.findByNicknameContaining(searchText);
    }

    public UserDocument findById(Long userId) {
        UserDocument userDocument = userElasticsearchRepository.findById(userId).orElse(null);
        if (userDocument == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND_ESDB);
        }

        return userDocument;
    }

    public Iterable<UserDocument> findAll() {
        return userElasticsearchRepository.findAll();
    }

    public void deleteById(Long userId) {
        userElasticsearchRepository.deleteById(userId);
    }

}