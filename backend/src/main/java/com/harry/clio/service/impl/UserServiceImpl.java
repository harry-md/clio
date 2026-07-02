package com.harry.clio.service.impl;

import com.harry.clio.dto.UserCreateRequest;
import com.harry.clio.dto.UserResponse;
import com.harry.clio.entity.User;
import com.harry.clio.entity.UserRole;
import com.harry.clio.exception.DuplicateResourceException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.UserMapper;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.security.CustomUser;
import com.harry.clio.service.CloudinaryService;
import com.harry.clio.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(@NonNull String username)
            throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        Set<GrantedAuthority> authorities =
                Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new CustomUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatar(),
                user.getRole().name(),
                authorities);
    }

    @Override
    public UserResponse register(UserCreateRequest request) {
        String avatarUrl = null;
        if (request.avatar() != null && !request.avatar().isEmpty()) {
            avatarUrl = cloudinaryService.upload(request.avatar());
        }
        final String finalAvatarUrl = avatarUrl;

        try {
            return transactionTemplate.execute((status) -> {
                if (userRepository.existsByUsername(request.username())) {
                    throw new DuplicateResourceException("Username này đã tồn tại");
                }

                User user = userMapper.toEntity(request);
                user.setPassword(passwordEncoder.encode(request.password()));
                user.setRole(UserRole.READER);
                user.setAvatar(finalAvatarUrl);
                return userMapper.toDto(userRepository.save(user));
            });
        } catch (Exception ex) {
            if (finalAvatarUrl != null) {
                cloudinaryService.delete(finalAvatarUrl);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponse getCurrentUser(int id) {
        return userMapper.toDto(userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user")));
    }
}
