package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserElasticsearchRepository extends ElasticsearchRepository<UserDocument, String> {

}
