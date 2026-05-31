package com.contactmaster.controller;

import com.contactmaster.common.ApiResponse;
import com.contactmaster.dto.ContactDtos.ContactResponse;
import com.contactmaster.dto.DashboardDtos.Statistics;
import com.contactmaster.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/statistics")
    public ApiResponse<Statistics> statistics(HttpServletRequest request) {
        return ApiResponse.ok(dashboardService.statistics(currentUserId(request)));
    }

    @GetMapping("/recent")
    public ApiResponse<List<ContactResponse>> recent(HttpServletRequest request) {
        return ApiResponse.ok(dashboardService.recent(currentUserId(request)));
    }

    @GetMapping("/favorites")
    public ApiResponse<List<ContactResponse>> favorites(HttpServletRequest request) {
        return ApiResponse.ok(dashboardService.favorites(currentUserId(request)));
    }

    @GetMapping("/birthdays")
    public ApiResponse<List<ContactResponse>> birthdays(HttpServletRequest request) {
        return ApiResponse.ok(dashboardService.birthdays(currentUserId(request)));
    }
}
