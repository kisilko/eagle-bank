package com.github.kisilko.eagle_bank.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private static final List<String> USER_DEFAULT_ROLES = List.of("USER");
    private static final List<String> ADMIN_DEFAULT_ROLES = List.of("ADMIN");

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public User creatUser(UserCreateRequest userCreateRequest) {
        String hashedPassword = passwordEncoder.encode(userCreateRequest.password());
        User newUser = User.builder()
                .name(userCreateRequest.name())
                .email(userCreateRequest.email())
                .password(hashedPassword)
                .roles(USER_DEFAULT_ROLES)
                .build();
        return usersRepository.save(newUser);
    }

    public User createAdmin(UserCreateRequest userCreateRequest) {
        String hashedPassword = passwordEncoder.encode(userCreateRequest.password());
        User newUser = User.builder()
                .name(userCreateRequest.name())
                .email(userCreateRequest.email())
                .password(hashedPassword)
                .roles(ADMIN_DEFAULT_ROLES)
                .build();
        return usersRepository.save(newUser);
    }

    public Optional<User> findById(Long userId) {
        return usersRepository.findById(userId);
    }

    public User updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setName(userUpdateRequest.name());

        return usersRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        usersRepository.deleteById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
}
