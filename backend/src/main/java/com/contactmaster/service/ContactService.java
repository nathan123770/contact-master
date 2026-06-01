package com.contactmaster.service;

import com.contactmaster.common.BusinessException;
import com.contactmaster.dto.ContactDtos.*;
import com.contactmaster.model.Contact;
import com.contactmaster.model.ContactGroup;
import com.contactmaster.model.ImportRecord;
import com.contactmaster.repository.ContactGroupRepository;
import com.contactmaster.repository.ContactRepository;
import com.contactmaster.repository.ImportRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ContactService {
    private final ContactRepository contactRepository;
    private final ContactGroupRepository groupRepository;
    private final ImportRecordRepository importRecordRepository;
    private final ReminderService reminderService;

    public ContactService(ContactRepository contactRepository,
                          ContactGroupRepository groupRepository,
                          ImportRecordRepository importRecordRepository,
                          ReminderService reminderService) {
        this.contactRepository = contactRepository;
        this.groupRepository = groupRepository;
        this.importRecordRepository = importRecordRepository;
        this.reminderService = reminderService;
    }

    public Page<ContactResponse> search(Long userId, String keyword, Long groupId, Boolean favorite, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
        Map<Long, String> groups = groupNameMap(userId);
        return contactRepository.search(userId, normalize(keyword), groupId, favorite, pageable)
                .map(contact -> ContactResponse.from(contact, groups.get(contact.getGroupId())));
    }

    public Page<ContactResponse> recycleBin(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
        Map<Long, String> groups = groupNameMap(userId);
        return contactRepository.findByUserIdAndDeletedTrueOrderByDeletedAtDesc(userId, pageable)
                .map(contact -> ContactResponse.from(contact, groups.get(contact.getGroupId())));
    }

    @Transactional
    public ContactResponse create(Long userId, ContactRequest request) {
        ensureGroupBelongsToUser(userId, request.groupId());
        if (contactRepository.existsByUserIdAndPhoneAndDeletedFalse(userId, request.phone())) {
            throw new BusinessException("该手机号已存在");
        }
        Contact contact = new Contact();
        contact.setUserId(userId);
        fill(contact, request);
        Contact saved = contactRepository.save(contact);
        return ContactResponse.from(saved, groupName(userId, saved.getGroupId()));
    }

    @Transactional
    public ContactResponse update(Long userId, Long id, ContactRequest request) {
        Contact contact = findOwned(userId, id);
        ensureGroupBelongsToUser(userId, request.groupId());
        if (contactRepository.existsByUserIdAndPhoneAndDeletedFalseAndIdNot(userId, request.phone(), id)) {
            throw new BusinessException("该手机号已存在");
        }
        fill(contact, request);
        return ContactResponse.from(contact, groupName(userId, contact.getGroupId()));
    }

    @Transactional
    public void softDelete(Long userId, Long id) {
        Contact contact = findOwned(userId, id);
        contact.setDeleted(true);
        contact.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    public void batchDelete(Long userId, BatchDeleteRequest request) {
        if (request.ids() == null || request.ids().isEmpty()) {
            throw new BusinessException("请选择要删除的联系人");
        }
        for (Long id : request.ids()) {
            softDelete(userId, id);
        }
    }

    @Transactional
    public ContactResponse toggleFavorite(Long userId, Long id) {
        Contact contact = findOwned(userId, id);
        contact.setFavorite(!contact.isFavorite());
        return ContactResponse.from(contact, groupName(userId, contact.getGroupId()));
    }

    @Transactional
    public ContactResponse restore(Long userId, Long id) {
        Contact contact = findOwned(userId, id);
        if (!contact.isDeleted()) {
            throw new BusinessException("联系人不在回收站");
        }
        if (contactRepository.existsByUserIdAndPhoneAndDeletedFalse(userId, contact.getPhone())) {
            throw new BusinessException("恢复失败：当前通讯录已存在相同手机号");
        }
        contact.setDeleted(false);
        contact.setDeletedAt(null);
        return ContactResponse.from(contact, groupName(userId, contact.getGroupId()));
    }

    @Transactional
    public void permanentDelete(Long userId, Long id) {
        Contact contact = findOwned(userId, id);
        if (!contact.isDeleted()) {
            throw new BusinessException("请先删除到回收站，再执行彻底删除");
        }
        reminderService.deleteByContact(userId, id);
        contactRepository.delete(contact);
    }

    @Transactional
    public ImportResult importCsv(Long userId, MultipartFile file) {
        List<ImportFailure> failures = new ArrayList<>();
        int success = 0;
        Set<String> phonesInFile = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            Map<String, Long> groupsByName = groupsByName(userId);
            while ((line = reader.readLine()) != null) {
                row++;
                if (row == 1 && line.toLowerCase(Locale.ROOT).contains("name") && line.toLowerCase(Locale.ROOT).contains("phone")) {
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] cells = splitCsv(line);
                try {
                    ContactRequest request = parseCsvRow(userId, cells, groupsByName);
                    if (!phonesInFile.add(request.phone())) {
                        throw new BusinessException("文件内手机号重复");
                    }
                    create(userId, request);
                    success++;
                } catch (Exception ex) {
                    failures.add(new ImportFailure(row, ex.getMessage(), line));
                }
            }
        } catch (IOException ex) {
            throw new BusinessException("读取文件失败");
        }

        ImportRecord record = new ImportRecord();
        record.setUserId(userId);
        record.setFileName(file.getOriginalFilename() == null ? "contacts.csv" : file.getOriginalFilename());
        record.setSuccessCount(success);
        record.setFailureCount(failures.size());
        importRecordRepository.save(record);
        return new ImportResult(success, failures.size(), failures);
    }

    public String exportCsv(Long userId, String keyword) {
        Map<Long, String> groups = groupNameMap(userId);
        StringBuilder builder = new StringBuilder("name,phone,email,group,company,position,address,birthday,remark,favorite\n");
        for (Contact contact : contactRepository.exportRows(userId, normalize(keyword))) {
            builder.append(csv(contact.getName())).append(',')
                    .append(csv(contact.getPhone())).append(',')
                    .append(csv(contact.getEmail())).append(',')
                    .append(csv(groups.get(contact.getGroupId()))).append(',')
                    .append(csv(contact.getCompany())).append(',')
                    .append(csv(contact.getPosition())).append(',')
                    .append(csv(contact.getAddress())).append(',')
                    .append(csv(contact.getBirthday() == null ? "" : contact.getBirthday().toString())).append(',')
                    .append(csv(contact.getRemark())).append(',')
                    .append(contact.isFavorite() ? "Y" : "N").append('\n');
        }
        return builder.toString();
    }

    private Contact findOwned(Long userId, Long id) {
        return contactRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("联系人不存在"));
    }

    private void fill(Contact contact, ContactRequest request) {
        contact.setGroupId(request.groupId());
        contact.setName(request.name().trim());
        contact.setPhone(request.phone().trim());
        contact.setEmail(normalize(request.email()));
        contact.setCompany(normalize(request.company()));
        contact.setPosition(normalize(request.position()));
        contact.setAddress(normalize(request.address()));
        contact.setBirthday(request.birthday());
        contact.setRemark(normalize(request.remark()));
        contact.setAvatarData(normalize(request.avatarData()));
        contact.setFavorite(request.favorite());
    }

    private ContactRequest parseCsvRow(Long userId, String[] cells, Map<String, Long> groupsByName) {
        if (cells.length < 2) {
            throw new BusinessException("至少需要姓名和手机号");
        }
        String name = cell(cells, 0);
        String phone = cell(cells, 1);
        if (name.isBlank()) {
            throw new BusinessException("姓名不能为空");
        }
        if (!phone.matches("^[0-9+\\- ]{6,20}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        LocalDate birthday = null;
        String birthdayText = cell(cells, 7);
        if (!birthdayText.isBlank()) {
            birthday = LocalDate.parse(birthdayText);
        }
        Long groupId = groupIdFromCsv(userId, cell(cells, 3), groupsByName);
        return new ContactRequest(
                groupId,
                name,
                phone,
                cell(cells, 2),
                cell(cells, 4),
                cell(cells, 5),
                cell(cells, 6),
                birthday,
                cell(cells, 8),
                null,
                "Y".equalsIgnoreCase(cell(cells, 9)) || "true".equalsIgnoreCase(cell(cells, 9))
        );
    }

    private Long groupIdFromCsv(Long userId, String groupName, Map<String, Long> groupsByName) {
        if (groupName == null || groupName.isBlank()) {
            return null;
        }
        String key = groupName.trim();
        Long existing = groupsByName.get(key);
        if (existing != null) {
            return existing;
        }
        ContactGroup group = new ContactGroup();
        group.setUserId(userId);
        group.setName(key);
        ContactGroup saved = groupRepository.save(group);
        groupsByName.put(saved.getName(), saved.getId());
        return saved.getId();
    }

    private String[] splitCsv(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cells.add(current.toString().trim());
        return cells.toArray(String[]::new);
    }

    private String cell(String[] cells, int index) {
        return index < cells.length ? cells[index].trim() : "";
    }

    private void ensureGroupBelongsToUser(Long userId, Long groupId) {
        if (groupId == null) {
            return;
        }
        groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException("分组不存在"));
    }

    private Map<Long, String> groupNameMap(Long userId) {
        return groupRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .collect(Collectors.toMap(ContactGroup::getId, ContactGroup::getName));
    }

    private Map<String, Long> groupsByName(Long userId) {
        return groupRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .collect(Collectors.toMap(ContactGroup::getName, ContactGroup::getId));
    }

    private String groupName(Long userId, Long groupId) {
        if (groupId == null) {
            return null;
        }
        return groupRepository.findByIdAndUserId(groupId, userId).map(ContactGroup::getName).orElse(null);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
