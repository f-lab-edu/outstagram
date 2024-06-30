package com.outstagram.outstagram.service;

import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.repository.PostElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostElasticsearchService {

    private final PostElasticsearchRepository postElasticsearchRepository;

    public void save(PostDocument post) {
        postElasticsearchRepository.save(post);
    }

    public PostDocument findById(Long postId) {
        PostDocument post = postElasticsearchRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new ApiException(ErrorCode.POST_NOT_FOUND_ESDB);
        }

        return post;
    }

    public Iterable<PostDocument> findAll() {
        return postElasticsearchRepository.findAll();
    }

    public List<PostDocument> findByKeyword(String searchTerm) {
        return postElasticsearchRepository.findByContentsContaining(searchTerm);
    }

    public void deleteById(Long postId) {
        postElasticsearchRepository.deleteById(postId);
    }
}
