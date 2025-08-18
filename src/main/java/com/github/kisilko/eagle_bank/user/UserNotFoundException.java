package com.github.kisilko.eagle_bank.user;
class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User %s not found".formatted(userId));
    }
}
