package com.github.kisilko.eagle_bank.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final List<String> USER_DEFAULT_ROLES = List.of("USER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateRequest userCreateRequest) {
        String hashedPassword = passwordEncoder.encode(userCreateRequest.password());
        User newUser = User.builder()
                .name(userCreateRequest.name())
                .email(userCreateRequest.email())
                .password(hashedPassword)
                .roles(USER_DEFAULT_ROLES)
                .build();
        return userRepository.save(newUser);
    }

    @PreAuthorize("principal.getId().equals(#userId)")
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @PreAuthorize("principal.getId().equals(#userId)")
    public User updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setName(userUpdateRequest.name());

        return userRepository.save(user);
    }

    @PreAuthorize("principal.getId().equals(#userId)")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean notExistsById(Long userId) {
        return !existsById(userId);
    }
}
