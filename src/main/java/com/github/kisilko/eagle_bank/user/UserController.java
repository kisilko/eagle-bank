package com.github.kisilko.eagle_bank.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @Operation(summary = "Get user details", description = "Returns user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("{userId}")
    public ResponseEntity<EntityModel<User>> userDetails(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();

        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!userService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(userModelAssembler.toModel(user));
    }

    @Operation(summary = "Create user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<EntityModel<User>> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        User newUser = userService.createUser(userCreateRequest);
        EntityModel<User> userModel = userModelAssembler.toModel(newUser);
        return ResponseEntity.created(userModel.getRequiredLink("self").toUri())
                .body(userModel);
    }

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PatchMapping("/{userId}")
    public ResponseEntity<EntityModel<User>> updateUserDetails(@PathVariable Long userId,
                                               @RequestBody UserUpdateRequest userUpdateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();

        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!userService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User updatedUser = userService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(userModelAssembler.toModel(updatedUser));
    }


    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseEntity<Void>> deleteUser(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();

        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!userService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
