package com.sports.service;

import com.sports.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExcelService {
    List<User> importStudents(MultipartFile file) throws Exception;
    List<User> importTeachers(MultipartFile file) throws Exception;
} 