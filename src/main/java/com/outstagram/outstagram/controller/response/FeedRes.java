package com.outstagram.outstagram.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedRes implements Serializable {

    private List<FeedPost> feedPostList;
    private Boolean hasNext;

}
