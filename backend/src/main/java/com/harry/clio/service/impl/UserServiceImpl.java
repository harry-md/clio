package com.harry.clio.service.impl;

import com.harry.clio.dto.CustomUser;
import com.harry.clio.dto.user.CreateUserRequest;
import com.harry.clio.dto.user.UserResponse;
import com.harry.clio.entity.SubscriptionStatus;
import com.harry.clio.entity.User;
import com.harry.clio.entity.UserRole;
import com.harry.clio.exception.BadRequestException;
import com.harry.clio.exception.ResourceNotFoundException;
import com.harry.clio.mapper.UserMapper;
import com.harry.clio.repository.SubscriptionRepository;
import com.harry.clio.repository.UserRepository;
import com.harry.clio.service.UserService;

import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SubscriptionRepository subscriptionRepository;

    private User getUserOrThrow(int id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
    }

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
    public UserResponse register(CreateUserRequest request) {
        String avatarUrl = null;
        if (request.avatar() != null && !request.avatar().isEmpty()) {
            avatarUrl = cloudinaryService.upload(request.avatar());
        }

        if (userRepository.existsByUsername(request.username())) {
            if (avatarUrl != null) cloudinaryService.delete(avatarUrl);
            throw new BadRequestException("Username này đã tồn tại");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.READER);
        if (avatarUrl != null) {
            user.setAvatar(avatarUrl);
        }

        try {
            return userMapper.toDto(userRepository.save(user), false);
        } catch (Exception ex) {
            cloudinaryService.delete(avatarUrl);
            throw ex;
        }
    }

    @Override
    public UserResponse getUserById(int id) {
        boolean isSubscribed =
                subscriptionRepository.existsByUserIdAndStatus(id, SubscriptionStatus.ACTIVE);
        return userMapper.toDto(getUserOrThrow(id), isSubscribed);
    }
}
