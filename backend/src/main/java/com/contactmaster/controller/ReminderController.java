package com.contactmaster.controller;

import com.contactmaster.common.ApiResponse;
import com.contactmaster.common.PageResponse;
import com.contactmaster.dto.ReminderDtos.*;
import com.contactmaster.service.ReminderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController extends BaseController {
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReminderResponse>> list(HttpServletRequest request,
                                                            @RequestParam(required = false) String status,
                                                            @RequestParam(required = false) ReminderType type,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) Long contactId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(reminderService.search(
                currentUserId(request),
                status,
                type,
                keyword,
                contactId,
                page,
                size
        )));
    }

    @PostMapping
    public ApiResponse<ReminderResponse> create(HttpServletRequest request, @Valid @RequestBody ReminderRequest body) {
        return ApiResponse.ok("提醒已创建", reminderService.create(currentUserId(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReminderResponse> update(HttpServletRequest request,
                                                @PathVariable Long id,
                                                @Valid @RequestBody ReminderRequest body) {
        return ApiResponse.ok("提醒已更新", reminderService.update(currentUserId(request), id, body));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<ReminderResponse> complete(HttpServletRequest request,
                                                  @PathVariable Long id,
                                                  @RequestBody CompleteReminderRequest body) {
        return ApiResponse.ok(reminderService.complete(currentUserId(request), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        reminderService.delete(currentUserId(request), id);
        return ApiResponse.ok("提醒已删除", null);
    }
}
