package com.outstagram.outstagram.repository;

import com.outstagram.outstagram.dto.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserElasticsearchRepository extends ElasticsearchRepository<UserDocument, Long> {
    List<UserDocument> findByNicknameContaining(String keyword);
}
