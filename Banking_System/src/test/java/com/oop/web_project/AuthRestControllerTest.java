package com.oop.web_project;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptionHandler.GlobalExceptionHandler;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.restController.AuthRestController;
import com.oop.web_project.services.AuthService;
import com.oop.web_project.services.CustomerService;
import com.oop.web_project.services.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuthRestControllerTest.ValidatorConfig.class})
class AuthRestControllerTest {

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
    private AuthService authService;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerApiMapper customerApiMapper;

    @MockitoBean
    private JWTService jwtService;

    @Test
    void testRegisterCustomerValidRequestReturnsCreated() throws Exception {
        when(customerApiMapper.toCustomerOnRegistration(any())).thenReturn(mock(Customer.class));

        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "phoneNumber" : "232324234",
                  "address" : "someaddress",
                  "dateOfBirth" : "2001-12-12",
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string("The Customer has been registered successfully."));
    }

    @Test
    void testRegisterCustomerValidRequestCallsCustomerService() throws Exception {
        Customer customer = mock(Customer.class);
        when(customerApiMapper.toCustomerOnRegistration(any())).thenReturn(customer);

        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "phoneNumber" : "232324234",
                  "address" : "someaddress",
                  "dateOfBirth" : "2001-12-12",
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));

        verify(customerService, times(1)).registerCustomer(customer);
    }

    @Test
    void testRegisterCustomerValidRequestCallsMapper() throws Exception {
        when(customerApiMapper.toCustomerOnRegistration(any())).thenReturn(mock(Customer.class));

        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "phoneNumber" : "232324234",
                  "address" : "someaddress",
                  "dateOfBirth" : "2001-12-12",
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());


    }

    @Test
    void testLoginCustomerValidCredentialsReturnsOk() throws Exception {
        when(authService.authenticateCustomer("john@example.com", "Password1!")).thenReturn("jwt-token");

        String body = """
                {
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginCustomerValidCredentialsReturnsTokenInResponse() throws Exception {
        when(authService.authenticateCustomer("john@example.com", "Password1!")).thenReturn("jwt-token");

        String body = """
                {
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$['Generated JWT token']").value("jwt-token"));
    }

    @Test
    void testLoginCustomerValidCredentialsReturnsStatusMessage() throws Exception {
        when(authService.authenticateCustomer(anyString(), anyString())).thenReturn("jwt-token");

        String body = """
                {
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$['Status message']").value("Authentication was successful!"));
    }

    @Test
    void testLoginCustomerCallsAuthService() throws Exception {
        when(authService.authenticateCustomer("john@example.com", "Password1!")).thenReturn("jwt-token");

        String body = """
                {
                  "email": "john@example.com",
                  "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));

        verify(authService, times(1)).authenticateCustomer("john@example.com", "Password1!");
    }

    @Test
    void testValidateCustomerReturnsOk() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isOk());
    }

    @Test
    void testValidateCustomerReturnsAuthenticatedMessage() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(content().string("User is authenticated!"));
    }
}
