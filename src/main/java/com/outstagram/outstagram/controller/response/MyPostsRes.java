package com.outstagram.outstagram.controller.response;

import com.outstagram.outstagram.dto.MyPostDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPostsRes {

    private List<MyPostDTO> postList;
    private boolean hasNext;

}
