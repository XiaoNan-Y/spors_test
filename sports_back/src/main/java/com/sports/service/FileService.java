package com.sports.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     * @param file 要上传的文件
     * @return 文件访问URL
     * @throws Exception 上传过程中的异常
     */
    String uploadFile(MultipartFile file) throws Exception;
} 