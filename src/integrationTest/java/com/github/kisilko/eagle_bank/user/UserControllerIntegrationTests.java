package com.github.kisilko.eagle_bank.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTests {

    private final MockMvcTester mockMvcTester;

    private final UserService userService;
    private final UserRepository userRepository;

    public UserControllerIntegrationTests(@Autowired MockMvcTester mockMvcTester,
                                          @Autowired UserService userService,
                                          @Autowired UserRepository userRepository) {
        this.mockMvcTester = mockMvcTester;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    void itCreatesValidNewUser() {
        String newUserJson = """
                    {
                        "name": "Alice Johnson",
                        "email": "alice.johnson@example.com",
                        "password": "secret_pass"
                    }""";

        MvcTestResult testResult = mockMvcTester
                .post()
                .uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(UserResponse.class)
                .satisfies(response -> {
                    assertThat(response.id()).isPositive();
                    assertThat(response.name()).isEqualTo("Alice Johnson");
                    assertThat(response.email()).isEqualTo("alice.johnson@example.com");
                });
    }

    @Test
    void itRespondsWithBadRequestIfRequiredDataAreMissing() {
        String invalidUserJson = "{}";

        MvcTestResult testResult = mockMvcTester
                .post()
                .uri("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .satisfies(response -> {
                    assertThat(response).isLenientlyEqualTo("""
                            {
                                "errors": [
                                    "Missing required field: name",
                                    "Missing required field: email",
                                    "Missing required field: password"
                                ]
                            }"""
                    );
                });
    }

    @Test
    void itFetchesTheExistingUserDetails() {
        User existingUser = userService.createUser(new UserCreateRequest("John Doe", "john.doe@example.com", "pass"));

        MvcTestResult testResult = mockMvcTester
                .get()
                .uri("/v1/users/{userId}", existingUser.getId())
                .header("Authorization", "Bearer " + getToken("john.doe@example.com", "pass"))
                .exchange();

        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .convertTo(UserResponse.class)
                .satisfies(response -> {
                    assertThat(response.id()).isEqualTo(existingUser.getId());
                    assertThat(response.name()).isEqualTo(existingUser.getName());
                    assertThat(response.email()).isEqualTo(existingUser.getEmail());
                });
    }

    @Test
    void itReturnsForbiddenWhenAccessingAnotherUsersData() {
        User existingUser1 = userService.createUser(new UserCreateRequest("John Doe", "john.doe@example.com", "pass"));
        User existingUser2 = userService.createUser(new UserCreateRequest("Carlos Rivera", "carlos.rivera@example.com", "pass"));

        String jwtForUser1 = getToken("john.doe@example.com", "pass");

        MvcTestResult testResult = mockMvcTester
                .get()
                .uri("/v1/users/{userId}", existingUser2.getId()) // user1 is checking data of user2
                .header("Authorization", "Bearer " + jwtForUser1)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void itReturnsNotFoundWhenUserDoesNotExist() {
        userService.createUser(new UserCreateRequest("Just User", "user@example.com", "some_pass"));
        var nonExistentUserId = 42;

        MvcTestResult testResult = mockMvcTester
                .get()
                .uri("/v1/users/{userId}", nonExistentUserId)
                .header("Authorization", "Bearer " + getToken("user@example.com", "some_pass"))
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(error -> {
                    assertThat(error.getDetail()).isEqualTo("User 42 not found");
                });
    }

    @Test
    void itUpdatesUserDetailsWhenPatchRequestIsValid() {
        User existingUser = userService.createUser(new UserCreateRequest("John Doe", "john.doe@example.com", "pass"));
        String newUserName = "Bob Smith"; // let's assume we allow to change name
        String newUserDetails = "{\"name\": \"%s\"}".formatted(newUserName);

        MvcTestResult testResult = mockMvcTester
                .patch()
                .uri("/v1/users/{userId}", existingUser.getId())
                .header("Authorization", "Bearer " + getToken("john.doe@example.com", "pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserDetails)
                .exchange();

        assertThat(testResult)
                .hasStatusOk()
                .bodyJson()
                .convertTo(UserResponse.class)
                .satisfies(response -> {
                    assertThat(response.id()).isEqualTo(existingUser.getId());
                    assertThat(response.name()).isEqualTo(newUserName);
                    assertThat(response.email()).isEqualTo(existingUser.getEmail());
                });
        User userFromDb = userRepository.findById(existingUser.getId()).orElseThrow(() -> new AssertionError("Missing user data in db"));
        assertThat(userFromDb.getName()).isEqualTo(newUserName);
    }

    @Test
    void itReturnsForbiddenWhenPatchingAnotherUsersData() {
        User existingUser1 = userService.createUser(new UserCreateRequest("John Doe", "john.doe@example.com", "pass"));
        User existingUser2 = userService.createUser(new UserCreateRequest("Carlos Rivera", "carlos.rivera@example.com", "pass"));

        String jwtForUser1 = getToken("john.doe@example.com", "pass");
        String newUserDetails = "{\"name\": \"Bob Smith\"}";

        MvcTestResult testResult = mockMvcTester
                .patch()
                .uri("/v1/users/{userId}", existingUser2.getId()) // user1 is patching user2
                .header("Authorization", "Bearer " + jwtForUser1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserDetails)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void itReturnsNotFoundWhenPatchingNonExistingUser() {
        userService.createUser(new UserCreateRequest("test user", "testuser@example.com", "pass"));
        var nonExistentUserId = 42;
        String newUserName = "Bob Smith"; // let's assume we allow to change name
        String newUserDetails = "{\"name\": \"%s\"}".formatted(newUserName);

        MvcTestResult testResult = mockMvcTester
                .patch()
                .uri("/v1/users/{userId}", nonExistentUserId)
                .header("Authorization", "Bearer " + getToken("testuser@example.com", "pass"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserDetails)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(error -> {
                    assertThat(error.getDetail()).isEqualTo("User 42 not found");
                });
    }

    @Test
    void itDeletesExistingUserWithoutBankAccount() {
        User existingUser = userService.createUser(new UserCreateRequest("Carlos Rivera", "carlos.rivera@example.com", "pass"));

        MvcTestResult testResult = mockMvcTester
                .delete()
                .uri("/v1/users/{userId}", existingUser.getId())
                .header("Authorization", "Bearer " + getToken("carlos.rivera@example.com", "pass"))
                .exchange();

        assertThat(testResult).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(userRepository.existsById(existingUser.getId())).isFalse();
    }

    @Test
    void itReturnsForbiddenWhenDeletingAnotherUsers() {
        User existingUser1 = userService.createUser(new UserCreateRequest("John Doe", "john.doe@example.com", "pass"));
        User existingUser2 = userService.createUser(new UserCreateRequest("Carlos Rivera", "carlos.rivera@example.com", "pass"));

        String jwtForUser1 = getToken("john.doe@example.com", "pass");

        MvcTestResult testResult = mockMvcTester
                .delete()
                .uri("/v1/users/{userId}", existingUser2.getId()) // user1 is deleting user2
                .header("Authorization", "Bearer " + jwtForUser1)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void itReturnsNotFoundWhenDeletingNonExistingUser() {
        userService.createUser(new UserCreateRequest("New User", "newuser@example.com", "new_pass"));
        var nonExistentUserId = 999;

        MvcTestResult testResult = mockMvcTester
                .delete()
                .uri("/v1/users/{userId}", nonExistentUserId)
                .header("Authorization", "Bearer " + getToken("newuser@example.com", "new_pass"))
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .convertTo(ProblemDetail.class)
                .satisfies(error -> {
                    assertThat(error.getDetail()).isEqualTo("User 999 not found");
                });
    }

    private String getToken(String email, String password) {

        MvcTestResult authResult = mockMvcTester
                .post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "%s",
                            "password": "%s"
                        }""".formatted(email, password)
                )
                .exchange();

        try {
            return authResult.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
