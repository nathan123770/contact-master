package com.contactmaster.controller;

import com.contactmaster.common.ApiResponse;
import com.contactmaster.common.PageResponse;
import com.contactmaster.dto.ContactDtos.*;
import com.contactmaster.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/contacts")
public class ContactController extends BaseController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ContactResponse>> list(HttpServletRequest request,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long groupId,
                                                           @RequestParam(required = false) Boolean favorite,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(contactService.search(currentUserId(request), keyword, groupId, favorite, page, size)));
    }

    @PostMapping
    public ApiResponse<ContactResponse> create(HttpServletRequest request, @Valid @RequestBody ContactRequest body) {
        return ApiResponse.ok("联系人已创建", contactService.create(currentUserId(request), body));
    }

    @PutMapping("/{id}")
    public ApiResponse<ContactResponse> update(HttpServletRequest request,
                                               @PathVariable Long id,
                                               @Valid @RequestBody ContactRequest body) {
        return ApiResponse.ok("联系人已更新", contactService.update(currentUserId(request), id, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        contactService.softDelete(currentUserId(request), id);
        return ApiResponse.ok("联系人已移入回收站", null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(HttpServletRequest request, @RequestBody BatchDeleteRequest body) {
        contactService.batchDelete(currentUserId(request), body);
        return ApiResponse.ok("联系人已批量移入回收站", null);
    }

    @PutMapping("/{id}/favorite")
    public ApiResponse<ContactResponse> favorite(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.ok(contactService.toggleFavorite(currentUserId(request), id));
    }

    @GetMapping("/recycle-bin")
    public ApiResponse<PageResponse<ContactResponse>> recycleBin(HttpServletRequest request,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.from(contactService.recycleBin(currentUserId(request), page, size)));
    }

    @PutMapping("/{id}/restore")
    public ApiResponse<ContactResponse> restore(HttpServletRequest request, @PathVariable Long id) {
        return ApiResponse.ok("联系人已恢复", contactService.restore(currentUserId(request), id));
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> permanentDelete(HttpServletRequest request, @PathVariable Long id) {
        contactService.permanentDelete(currentUserId(request), id);
        return ApiResponse.ok("联系人已彻底删除", null);
    }

    @PostMapping("/import")
    public ApiResponse<ImportResult> importCsv(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("导入完成", contactService.importCsv(currentUserId(request), file));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(HttpServletRequest request, @RequestParam(required = false) String keyword) {
        byte[] body = ("\uFEFF" + contactService.exportCsv(currentUserId(request), keyword)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
