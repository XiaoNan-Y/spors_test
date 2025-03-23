package com.sports.service.impl;

import com.sports.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url}")
    private String accessUrl;

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 确保目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            log.info("Saving file to: {}", filePath);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回访问URL
            String fileUrl = accessUrl + "/" + filename;
            log.info("File URL: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            throw new Exception("文件上传失败：" + e.getMessage());
        }
    }
} 