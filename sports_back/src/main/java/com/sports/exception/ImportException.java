package com.sports.exception;

import com.sports.entity.User;
import java.util.List;

public class ImportException extends RuntimeException {
    private List<String> errors;
    private List<User> successUsers;

    public ImportException(String message, List<String> errors, List<User> successUsers) {
        super(message);
        this.errors = errors;
        this.successUsers = successUsers;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<User> getSuccessUsers() {
        return successUsers;
    }
} 