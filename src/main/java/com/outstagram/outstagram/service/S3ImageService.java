package com.outstagram.outstagram.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.outstagram.outstagram.dto.ImageDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.mapper.ImageMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class S3ImageService extends AbstractImageService{

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    //private final static String UPLOAD_PATH = "https://outstagram-s3.s3.ap-northeast-2.amazonaws.com/";

    public S3ImageService(ImageMapper imageMapper, AmazonS3 amazonS3) {
        super(imageMapper);
        this.amazonS3 = amazonS3;
    }

    @Override
    public String uploadImage(MultipartFile image) {
        return upload(image);
    }

    @Override
    public void deleteRealImages(List<ImageDTO> deletedImages) {
        for (ImageDTO image : deletedImages) {
            deleteImageFromS3(image.getSavedImgName());
        }

    }


    public String upload(MultipartFile image) {
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new ApiException(ErrorCode.EMPTY_FILE_EXCEPTION);
        }
        return this.uploadS3Image(image);
    }

    private String uploadS3Image(MultipartFile image) {
        this.validateImageFileExtention(image.getOriginalFilename());
        try {
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new ApiException(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    /**
     * 이미지 파일의 확장자 명이 올바른지 확인
     */
    private void validateImageFileExtention(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new ApiException(ErrorCode.NO_FILE_EXTENTION);
        }

        String extention = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png", "gif");

        if (!allowedExtentionList.contains(extention)) {
            throw new ApiException(ErrorCode.INVALID_FILE_EXTENTION);
        }
    }

    /**
     * 실제 S3에 이미지 업로드하는 메서드
     */
    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); //원본 파일 명
        String extention = originalFilename.substring(originalFilename.lastIndexOf(".")); //확장자 명

        String s3FileName =
            UUID.randomUUID().toString().substring(0, 10) + originalFilename; //실제 S3에 저장될 파일 명

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is); // image를 byte 배열로 변환

        ObjectMetadata metadata = new ObjectMetadata(); // metadata 생성
        metadata.setContentType("image/" + extention);
        metadata.setContentLength(bytes.length);

        // S3에 요청할 때 사용할 byteInputStream 생성
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest =
                new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata);
            //.withCannedAcl(CannedAccessControlList.PublicRead);
            // 실제 S3에 이미지 업로드하는 코드
            amazonS3.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new ApiException(e, ErrorCode.PUT_OBJECT_EXCEPTION);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        return amazonS3.getUrl(bucketName, s3FileName).toString();
    }

    public void deleteImageFromS3(String imgUrl) {
        String key = getKeyFromImageUrl(imgUrl);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    private String getKeyFromImageUrl(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        } catch (MalformedURLException e) {
            throw new ApiException(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }



}
