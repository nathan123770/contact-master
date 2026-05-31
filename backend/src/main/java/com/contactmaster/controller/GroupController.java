package com.contactmaster.controller;

import com.contactmaster.common.ApiResponse;
import com.contactmaster.dto.GroupDtos.*;
import com.contactmaster.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController extends BaseController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ApiResponse<List<GroupResponse>> list(HttpServletRequest request) {
        return ApiResponse.ok(groupService.list(currentUserId(request)));
    }

    @PostMapping
    public ApiResponse<GroupResponse> create(HttpServletRequest request, @Valid @RequestBody GroupRequest body) {
        return ApiResponse.ok("分组已创建", groupService.create(currentUserId(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<GroupResponse> update(HttpServletRequest request,
                                             @PathVariable Long id,
                                             @Valid @RequestBody GroupRequest body) {
        return ApiResponse.ok("分组已更新", groupService.update(currentUserId(request), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        groupService.delete(currentUserId(request), id);
        return ApiResponse.ok("分组已删除", null);
    }
}
