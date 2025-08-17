package com.github.kisilko.eagle_bank.users;

import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
class UsersController {

    private final UsersService usersService;
    private final UsersModelAssembler usersModelAssembler;

    public UsersController(UsersService usersService, UsersModelAssembler usersModelAssembler) {
        this.usersService = usersService;
        this.usersModelAssembler = usersModelAssembler;
    }

    @GetMapping("{userId}")
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public EntityModel<User> userDetails(@PathVariable Long userId) {
        SecurityContextHolder.getContext().getAuthentication();
        User user = usersService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return usersModelAssembler.toModel(user);
    }

    @PostMapping
    public ResponseEntity<EntityModel<User>> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        User newUser = usersService.creatUser(userCreateRequest);
        EntityModel<User> userModel = usersModelAssembler.toModel(newUser);
        return ResponseEntity.created(userModel.getRequiredLink("self").toUri())
                .body(userModel);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public EntityModel<User> updateUserDetails(@PathVariable Long userId,
                                               @RequestBody UserUpdateRequest userUpdateRequest) {
        User updatedUser = usersService.updateUser(userId, userUpdateRequest);
        return usersModelAssembler.toModel(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        usersService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
