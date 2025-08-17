package com.github.kisilko.eagle_bank.users;

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
class UsersController {

    private final UsersService usersService;
    private final UsersModelAssembler usersModelAssembler;

    public UsersController(UsersService usersService, UsersModelAssembler usersModelAssembler) {
        this.usersService = usersService;
        this.usersModelAssembler = usersModelAssembler;
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

        if (!usersService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!usersService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = usersService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(usersModelAssembler.toModel(user));
    }

    @Operation(summary = "Create user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping
    public ResponseEntity<EntityModel<User>> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        User newUser = usersService.createUser(userCreateRequest);
        EntityModel<User> userModel = usersModelAssembler.toModel(newUser);
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

        if (!usersService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!usersService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User updatedUser = usersService.updateUser(userId, userUpdateRequest);
        return ResponseEntity.ok(usersModelAssembler.toModel(updatedUser));
    }


    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseEntity<Void>> deleteUser(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails) auth.getPrincipal();

        if (!usersService.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (!usersService.isCurrentUser(userId, currentUser.getUsername())) { // in our case getUsername() returns email
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        usersService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
