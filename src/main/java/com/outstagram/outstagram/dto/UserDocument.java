package com.outstagram.outstagram.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class UserDocument {
    @Id
    private String id;

    private String nickname;

    private String email;

    private String imgUrl;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
