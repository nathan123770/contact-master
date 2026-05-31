package com.contactmaster.controller;

import com.contactmaster.config.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseController {
    protected Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
    }
}
