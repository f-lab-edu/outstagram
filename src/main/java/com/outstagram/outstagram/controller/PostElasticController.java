package com.outstagram.outstagram.controller;

import com.outstagram.outstagram.dto.PostDocument;
import com.outstagram.outstagram.service.PostElasticsearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/elasticsearch-posts")
public class PostElasticController {

    private final PostElasticsearchService postElasticsearchService;

    @GetMapping("/all")
    public ResponseEntity<Iterable<PostDocument>> getAllPostsInElasticDB() {
        return ResponseEntity.ok(postElasticsearchService.findAll());
    }

    @GetMapping()
    public ResponseEntity<List<PostDocument>> searchContentByKeyword(@RequestParam String searchText) {
        List<PostDocument> result = postElasticsearchService.findByKeyword(searchText);
        return ResponseEntity.ok(result);
    }

}