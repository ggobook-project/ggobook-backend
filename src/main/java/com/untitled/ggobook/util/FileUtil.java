package com.untitled.ggobook.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class FileUtil {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    public String uploadToS3(MultipartFile multipartFile){
        validateFileExtension(multipartFile);

        try{
            String storedName = generateUniqueFileName(multipartFile);

            ObjectMetadata objMeta = new ObjectMetadata();
            objMeta.setContentType(multipartFile.getContentType());
            InputStream inputStream = multipartFile.getInputStream();
            objMeta.setContentLength(inputStream.available());

            amazonS3.putObject(bucket, storedName, inputStream, objMeta);
            inputStream.close();

            return "https://s3.ap-northeast-2.amazonaws.com/" + bucket + "/" + storedName;

        }catch(IOException e){
            throw new RuntimeException("S3 파일 업로드 실패", e);

        }
    }

    public String deleteFromS3(String fileName){
        if (fileName == null || fileName.isBlank()) return null;

        String[] onlyFileName = fileName.split(".com/");
        String changeFileName = URLDecoder.decode(onlyFileName[1], StandardCharsets.UTF_8);

        amazonS3.deleteObject(new DeleteObjectRequest(this.bucket, changeFileName));

        return fileName;
    }

    public String generateUniqueFileName(MultipartFile multipartFile){
        String originalName = multipartFile.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }

    public void validateFileExtension(MultipartFile multipartFile){
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 확장자입니다. (jpg, jpeg, png, gif만 허용)");
        }
    }

}
