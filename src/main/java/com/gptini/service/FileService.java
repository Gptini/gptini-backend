package com.gptini.service;

import com.gptini.dto.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadResponse upload(MultipartFile file);
}
