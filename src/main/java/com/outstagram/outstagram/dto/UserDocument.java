package com.outstagram.outstagram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class UserDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer", searchAnalyzer = "nori_search_analyzer")
    private String nickname;

    @Field(type = FieldType.Text)
    private String email;

    @Field(type = FieldType.Text)
    private String imgUrl;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createDate;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updateDate;
}
