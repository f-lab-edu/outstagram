package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.UserDocument;
import com.outstagram.outstagram.mapper.UserElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserElasticSearchService {
    private final UserElasticsearchRepository userElasticsearchRepository;

    public void save(UserDocument document) {
        userElasticsearchRepository.save(document);
    }

    public void deleteDocument(String id) {

    }


}
