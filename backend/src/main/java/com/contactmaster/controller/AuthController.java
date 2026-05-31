package com.contactmaster.controller;

import com.contactmaster.common.ApiResponse;
import com.contactmaster.dto.AuthDtos.*;
import com.contactmaster.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("登录成功", authService.login(request));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(HttpServletRequest servletRequest,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUserId(servletRequest), request);
        return ApiResponse.ok("密码修改成功", null);
    }
}
