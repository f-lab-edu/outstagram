package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.repository.UserElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserElasticsearchService {

    private final UserElasticsearchRepository userElasticsearchRepository;

    public void save(UserDocument document) {

        userElasticsearchRepository.save(document);
    }

    public List<UserDocument> findByNickname(String keyword) {
        return userElasticsearchRepository.findByNicknameContaining(keyword);
    }

}
