package com.contactmaster.service;

import com.contactmaster.common.BusinessException;
import com.contactmaster.dto.GroupDtos.*;
import com.contactmaster.model.ContactGroup;
import com.contactmaster.repository.ContactGroupRepository;
import com.contactmaster.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {
    private final ContactGroupRepository groupRepository;
    private final ContactRepository contactRepository;

    public GroupService(ContactGroupRepository groupRepository, ContactRepository contactRepository) {
        this.groupRepository = groupRepository;
        this.contactRepository = contactRepository;
    }

    public List<GroupResponse> list(Long userId) {
        return groupRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(group -> new GroupResponse(
                        group.getId(),
                        group.getName(),
                        contactRepository.countByUserIdAndGroupIdAndDeletedFalse(userId, group.getId()),
                        group.getCreatedAt()))
                .toList();
    }

    @Transactional
    public GroupResponse create(Long userId, GroupRequest request) {
        String name = request.name().trim();
        if (groupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException("分组名称已存在");
        }
        ContactGroup group = new ContactGroup();
        group.setUserId(userId);
        group.setName(name);
        ContactGroup saved = groupRepository.save(group);
        return new GroupResponse(saved.getId(), saved.getName(), 0, saved.getCreatedAt());
    }

    @Transactional
    public GroupResponse update(Long userId, Long id, GroupRequest request) {
        ContactGroup group = groupRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("分组不存在"));
        String name = request.name().trim();
        if (!group.getName().equals(name) && groupRepository.existsByUserIdAndName(userId, name)) {
            throw new BusinessException("分组名称已存在");
        }
        group.setName(name);
        return new GroupResponse(group.getId(), group.getName(),
                contactRepository.countByUserIdAndGroupIdAndDeletedFalse(userId, group.getId()),
                group.getCreatedAt());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ContactGroup group = groupRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("分组不存在"));
        if (contactRepository.countByUserIdAndGroupIdAndDeletedFalse(userId, id) > 0) {
            throw new BusinessException("该分组下还有联系人，不能删除");
        }
        groupRepository.delete(group);
    }
}
