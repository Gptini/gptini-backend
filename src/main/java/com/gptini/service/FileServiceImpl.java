package com.gptini.service;

import com.gptini.dto.response.FileUploadResponse;
import com.gptini.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            // 이미지
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/svg+xml",
            // GIF
            "image/gif",
            // 일반 파일
            "application/pdf",
            "application/zip",
            "application/x-zip-compressed",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "video/mp4",
            "video/quicktime",
            "audio/mpeg",
            "audio/wav"
    );

    @Override
    public FileUploadResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BusinessException.badRequest("지원하지 않는 파일 형식입니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);
        String key = "chat/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .acl("public-read")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

            return new FileUploadResponse(fileUrl, originalFileName);
        } catch (IOException e) {
            throw BusinessException.internalError("파일 업로드에 실패했습니다.");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
