package com.github.kisilko.eagle_bank.account;

import com.github.kisilko.eagle_bank.user.User;
import com.github.kisilko.eagle_bank.user.UserCreateRequest;
import com.github.kisilko.eagle_bank.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class BankBankAccountControllerIntegrationTests {

    private final MockMvcTester mockMvcTester;
    private final UserService userService;

    public BankBankAccountControllerIntegrationTests(@Autowired MockMvcTester mockMvcTester,
                                                     @Autowired UserService userService) {
        this.mockMvcTester = mockMvcTester;
        this.userService = userService;
    }

    @Test
    void loggedInUserCanCreateNewBankAccount() {
        User existingUser = userService.createUser(new UserCreateRequest("Jane Smith", "jane.smith@example.com", "pass123"));
        String accountCreationRequest = """
                {
                    "userId": "%s",
                    "accountType": "SAVINGS",
                    "currency": "USD"
                }""".formatted(existingUser.getId());

        MvcTestResult testResult = mockMvcTester
                .post()
                .uri("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getToken("jane.smith@example.com", "pass123"))
                .content(accountCreationRequest)
                .exchange();

        assertThat(testResult)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .convertTo(AccountResponse.class)
                .satisfies(response -> {
                    assertThat(response.id()).isPositive();
                    assertThat(response.userId()).isEqualTo(existingUser.getId());
                    assertThat(response.accountType()).isEqualTo("SAVINGS");
                    assertThat(response.currency()).isEqualTo("USD");
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
