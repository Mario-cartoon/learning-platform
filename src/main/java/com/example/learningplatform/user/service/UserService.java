package com.example.learningplatform.user.service;

import com.example.learningplatform.common.exception.ConflictException;
import com.example.learningplatform.course.repository.CourseRepository;
import com.example.learningplatform.user.dto.ProfileRequest;
import com.example.learningplatform.user.dto.ProfileResponse;
import com.example.learningplatform.user.dto.UserRequest;
import com.example.learningplatform.user.dto.UserResponse;
import com.example.learningplatform.user.entity.Profile;
import com.example.learningplatform.user.entity.Role;
import com.example.learningplatform.user.entity.User;
import com.example.learningplatform.user.repository.ProfileRepository;
import com.example.learningplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;

    public List<UserResponse> findAll(Role role) {
        List<User> users = role == null
                ? userRepository.findAll(Sort.by("id"))
                : userRepository.findAllByRoleOrderByIdAsc(role);
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse findById(Long id) {
        return UserResponse.from(userRepository.getOrThrow(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        ensureEmailIsFree(request.email(), null);
        User saved = userRepository.save(new User(request.name(), request.email(), request.role()));
        log.info("Создан пользователь id={} роль={}", saved.getId(), saved.getRole());
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.getOrThrow(id);
        ensureEmailIsFree(request.email(), id);
        boolean losesTeacherRole = user.getRole() == Role.TEACHER && request.role() != Role.TEACHER;
        if (losesTeacherRole && courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("Нельзя сменить роль преподавателю, за которым закреплены курсы");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
        log.info("Обновлён пользователь id={}", id);
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.getOrThrow(id);
        if (courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("Нельзя удалить преподавателя, за которым закреплены курсы. "
                    + "Сначала назначьте курсам другого преподавателя");
        }
        // профиль, записи на курсы, решения, результаты тестов и отзывы удалятся каскадом
        userRepository.delete(user);
        log.info("Удалён пользователь id={}", id);
    }

    public ProfileResponse getProfile(Long userId) {
        userRepository.getOrThrow(userId);
        return profileRepository.findByUserId(userId)
                .map(ProfileResponse::from)
                .orElseGet(() -> ProfileResponse.empty(userId));
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.getOrThrow(userId);
        Profile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            Profile created = new Profile();
            user.attachProfile(created);
            return created;
        });
        profile.setBio(request.bio());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setPhone(request.phone());
        return ProfileResponse.from(profileRepository.save(profile));
    }

    private void ensureEmailIsFree(String email, Long currentUserId) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ConflictException("Пользователь с email " + email + " уже существует");
                });
    }
}
