package com.outstagram.outstagram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class UserDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String nickname;

    @Field(type = FieldType.Text)
    private String email;

    @Field(type = FieldType.Text)
    private String imgUrl;
}
