package com.contactmaster.service;

import com.contactmaster.common.BusinessException;
import com.contactmaster.dto.AuthDtos.*;
import com.contactmaster.model.AppUser;
import com.contactmaster.model.ContactGroup;
import com.contactmaster.repository.ContactGroupRepository;
import com.contactmaster.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ContactGroupRepository groupRepository;
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, ContactGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("用户名已存在");
        }
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(hash(request.password()));
        AppUser saved = userRepository.save(user);
        createDefaultGroups(saved.getId());
        return issueToken(saved);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("账号或密码错误"));
        if (!user.getPasswordHash().equals(hash(request.password()))) {
            throw new BusinessException("账号或密码错误");
        }
        return issueToken(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
        if (!user.getPasswordHash().equals(hash(request.oldPassword()))) {
            throw new BusinessException("原密码不正确");
        }
        user.setPasswordHash(hash(request.newPassword()));
    }

    public Long resolveUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return sessions.get(token);
    }

    private AuthResponse issueToken(AppUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, user.getId());
        return new AuthResponse(user.getId(), user.getUsername(), user.getEmail(), token);
    }

    private void createDefaultGroups(Long userId) {
        for (String name : new String[]{"默认分组", "家人", "朋友", "同事"}) {
            ContactGroup group = new ContactGroup();
            group.setUserId(userId);
            group.setName(name);
            groupRepository.save(group);
        }
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(("contact-master:" + value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("密码加密失败");
        }
    }
}
