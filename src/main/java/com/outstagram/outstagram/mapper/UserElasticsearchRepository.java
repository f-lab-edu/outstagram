package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserDocument, String> {
    List<UserDocument> findByNicknameContaining(String keyword);
}
