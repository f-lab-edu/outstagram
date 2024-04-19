package com.outstagram.outstagram.mapper;

import com.outstagram.outstagram.dto.ImageDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMapper {
    void insertImages(List<ImageDTO> imageList);

}
