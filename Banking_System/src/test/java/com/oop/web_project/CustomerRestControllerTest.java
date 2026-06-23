package com.oop.web_project;

import com.oop.web_project.dto.responses.CustomerProfileResponse;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptionHandler.GlobalExceptionHandler;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyActiveException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyDeactivatedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerNotFoundException;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.restController.CustomerRestController;
import com.oop.web_project.services.CustomerService;
import com.oop.web_project.services.JWTService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CustomerRestControllerTest.ValidatorConfig.class})
class CustomerRestControllerTest {

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
    private CustomerService customerService;

    @MockitoBean
    private CustomerApiMapper customerApiMapper;

    @MockitoBean
    private JWTService jwtService;

    @Test
    void testGetCustomerProfileWithEmailReturnsOk() throws Exception {
        Customer customer = mock(Customer.class);
        CustomerProfileResponse response = new CustomerProfileResponse(
                "John", "Doe", LocalDate.of(2000, 1, 1),
                "123 Main St", "john@example.com", "1234567890", List.of());

        when(customerService.getCustomerByEmail("john@example.com")).thenReturn(customer);
        when(customerApiMapper.toProfileResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/customer/email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));

        verify(customerService).getCustomerByEmail("john@example.com");
        verify(customerApiMapper).toProfileResponse(customer);
    }

    @Test
    void testGetCustomerProfileWithEmailReturnsNotFound() throws Exception {
        when(customerService.getCustomerByEmail("missing@example.com"))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customer/email/missing@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    @Test
    void testGetCustomerProfilesByAccountReturnsOk() throws Exception {
        Customer customer = mock(Customer.class);
        CustomerProfileResponse response = new CustomerProfileResponse(
                "John", "Doe", LocalDate.of(2000, 1, 1),
                "123 Main St", "john@example.com", "1234567890", List.of());

        when(customerService.getCustomersByAccount(1L)).thenReturn(List.of(customer));
        when(customerApiMapper.toProfileResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/customer/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$[0].address").value("123 Main St"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("1234567890"));

        verify(customerService).getCustomersByAccount(1L);
        verify(customerApiMapper).toProfileResponse(customer);
    }

    @Test
    void testGetCustomerProfilesByAccountReturnsNotFound() throws Exception {
        when(customerService.getCustomersByAccount(99L))
                .thenThrow(new AccountNotFoundException("account cannot be found"));

        mockMvc.perform(get("/api/customer/account/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("account cannot be found"));
    }

    @Test
    void testGetCustomerProfileReturnsOk() throws Exception {
        Customer customer = mock(Customer.class);
        CustomerProfileResponse response = new CustomerProfileResponse(
                "John", "Doe", LocalDate.of(2000, 1, 1),
                "123 Main St", "john@example.com", "1234567890", List.of());

        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(customerApiMapper.toProfileResponse(customer)).thenReturn(response);

        mockMvc.perform(get("/api/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-01-01"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("1234567890"));

        verify(customerService).getCustomerById(1L);
        verify(customerApiMapper).toProfileResponse(customer);
    }

    @Test
    void testGetCustomerProfileReturnsNotFound() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customer/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    @Test
    void testUpdateCustomerProfileReturnsOk() throws Exception {
        Customer customer = mock(Customer.class);
        CustomerProfileResponse response = new CustomerProfileResponse(
                "Jane", "Smith", LocalDate.of(1995, 5, 5),
                "456 Oak Ave", "jane@example.com", "0987654321", List.of());

        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(customerApiMapper.toProfileResponse(customer)).thenReturn(response);

        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Smith",
                  "phoneNumber": "0987654321",
                  "address": "456 Oak Ave"
                }
                """;

        mockMvc.perform(put("/api/customer/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.phoneNumber").value("0987654321"))
                .andExpect(jsonPath("$.address").value("456 Oak Ave"));

        verify(customerService).updateCustomer(1L, "Jane", "Smith", "0987654321", "456 Oak Ave");
        verify(customerService).getCustomerById(1L);
    }

    @Test
    void testUpdateCustomerProfileReturnsNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).updateCustomer(eq(99L), any(), any(), any(), any());

        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Smith",
                  "phoneNumber": "0987654321",
                  "address": "456 Oak Ave"
                }
                """;

        mockMvc.perform(put("/api/customer/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    @Test
    void testDeactivateCustomerReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/customer/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(content().string("The customer has been deactivated successfully."));

        verify(customerService).deactivateCustomer(1L);
    }

    @Test
    void testDeactivateCustomerReturnsNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).deactivateCustomer(99L);

        mockMvc.perform(patch("/api/customer/99/deactivate"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    @Test
    void testDeactivateCustomerReturnsNotAcceptable() throws Exception {
        doThrow(new CustomerAlreadyDeactivatedException("Customer already deactivated"))
                .when(customerService).deactivateCustomer(1L);

        mockMvc.perform(patch("/api/customer/1/deactivate"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Customer already deactivated"));
    }

    @Test
    void testActivateCustomerReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/customer/1/activate"))
                .andExpect(status().isOk())
                .andExpect(content().string("The customer has been activated successfully."));

        verify(customerService).activateCustomer(1L);
    }

    @Test
    void testActivateCustomerReturnsNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).activateCustomer(99L);

        mockMvc.perform(patch("/api/customer/99/activate"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }

    @Test
    void testActivateCustomerReturnsNotAcceptable() throws Exception {
        doThrow(new CustomerAlreadyActiveException("Customer already active"))
                .when(customerService).activateCustomer(1L);

        mockMvc.perform(patch("/api/customer/1/activate"))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Customer already active"));
    }

    @Test
    void testDeleteCustomerReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/customer/1/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("The customer has been deleted successfully."));

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void testDeleteCustomerReturnsNotFound() throws Exception {
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(99L);

        mockMvc.perform(delete("/api/customer/99/delete"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer not found"));
    }
}