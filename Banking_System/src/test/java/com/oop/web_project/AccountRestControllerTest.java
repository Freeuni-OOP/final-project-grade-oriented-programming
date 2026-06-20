package com.oop.web_project;

import com.oop.web_project.dto.responses.AccountProfileResponse;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.exceptionHandler.GlobalExceptionHandler;
import com.oop.web_project.mapping.AccountApiMapper;
import com.oop.web_project.restController.AccountRestController;
import com.oop.web_project.services.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AccountRestControllerTest.ValidatorConfig.class})
class AccountRestControllerTest {

    @TestConfiguration
    static class ValidatorConfig {
        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountApiMapper accountMapper;

    @Test
    void testCreateAccountReturnsCreated() throws Exception {
        when(accountMapper.toAccount(any())).thenReturn(mock(Account.class));

        String body = """
                {
                  "customerId": 1,
                  "accountName": "Savings",
                  "currencyCode": "USD"
                }
                """;

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account has been successfully created."));

        verify(accountService).createAccount(any(Account.class));
    }

    @Test
    void testActivateAccountReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/account/1/activate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account has been successfully activated."));

        verify(accountService).activateAccount(1L);
    }

    @Test
    void testDeactivateAccountReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/account/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account has been successfully deactivated."));

        verify(accountService).deactivateAccount(1L);
    }

    @Test
    void testGetAccountByIdReturnsOk() throws Exception {
        Account account = mock(Account.class);
        AccountProfileResponse response = mock(AccountProfileResponse.class);

        when(accountService.selectAccountById(1L)).thenReturn(account);
        when(accountMapper.toProfileResponse(account)).thenReturn(response);

        mockMvc.perform(get("/api/account/1"))
                .andExpect(status().isOk());

        verify(accountService).selectAccountById(1L);
        verify(accountMapper).toProfileResponse(account);
    }

    @Test
    void testDeleteAccountReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/account/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account has been successfully deleted."));

        verify(accountService).deleteAccount(1L);
    }

    @Test
    void testGetAccountsByEmailReturnsOk() throws Exception {
        Account account = mock(Account.class);
        AccountSummaryResponse summaryResponse = mock(AccountSummaryResponse.class);

        when(accountService.selectAccountsByCustomerEmail("john@example.com")).thenReturn(List.of(account));
        when(accountMapper.toAccountSummaryResponse(account)).thenReturn(summaryResponse);

        mockMvc.perform(get("/api/account").param("customerEmail", "john@example.com"))
                .andExpect(status().isOk());

        verify(accountService).selectAccountsByCustomerEmail("john@example.com");
        verify(accountMapper).toAccountSummaryResponse(account);
    }

    @Test
    void testGetAccountsByEmailReturnsEmptyListWhenNoAccounts() throws Exception {
        when(accountService.selectAccountsByCustomerEmail("unknown@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/account").param("customerEmail", "unknown@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGetAccountsByCustomerIdReturnsOk() throws Exception {
        Account account = mock(Account.class);
        AccountProfileResponse profileResponse = mock(AccountProfileResponse.class);

        when(accountService.selectAccountsByCustomerId(1L)).thenReturn(List.of(account));
        when(accountMapper.toProfileResponse(account)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/account/customer/1"))
                .andExpect(status().isOk());

        verify(accountService).selectAccountsByCustomerId(1L);
        verify(accountMapper).toProfileResponse(account);
    }

    @Test
    void testGetAccountsByCustomerIdReturnsEmptyListWhenNoAccounts() throws Exception {
        when(accountService.selectAccountsByCustomerId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/account/customer/99"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testUpdateAccountReturnsOk() throws Exception {
        mockMvc.perform(put("/api/account/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("New Account Name"))
                .andExpect(status().isOk())
                .andExpect(content().string("successfully updated"));

        verify(accountService).updateAccount(1L, "New Account Name");
    }

    @Test
    void testRegisterCustomerToAccountReturnsOk() throws Exception {
        mockMvc.perform(put("/api/account/1/customers/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully registered"));

        verify(accountService).registerCustomerToAccount(1L, 2L);
    }

    @Test
    void testGetAccountBalanceByCurrencyReturnsOk() throws Exception {
        when(accountService.getAccountBalanceByCurrency(1L, "USD")).thenReturn(BigDecimal.valueOf(1500));

        mockMvc.perform(get("/api/account/1/balance").param("currencyCode", "USD"))
                .andExpect(status().isOk());

        verify(accountService).getAccountBalanceByCurrency(1L, "USD");
    }
}