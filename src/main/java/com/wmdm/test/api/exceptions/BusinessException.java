package com.wmdm.test.api.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String errorMessege) {
        super(errorMessege);
    }
}
