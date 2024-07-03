package com.outstagram.outstagram.repository;

import com.outstagram.outstagram.dto.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostElasticsearchRepository extends ElasticsearchRepository<PostDocument, Long> {
    List<PostDocument> findByContentsContaining(String keyword);
}
