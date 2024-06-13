package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.dto.FeedPostDTO;
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

    private List<FeedPostDTO> feedPostDTOList;
    private Boolean hasNext;

}
