package com.oop.web_project;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Role;
import com.oop.web_project.exceptions.customerExceptions.CustomerNotFoundException;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.services.CustomerService;
import com.oop.web_project.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testLoadUserByUsernameReturnsNonNullUserDetails() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(result);
    }

    @Test
    void testLoadUserByUsernameReturnsCorrectEmail() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    void testLoadUserByUsernameReturnsCorrectPassword() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertEquals("hashedPass", result.getPassword());
    }

    @Test
    void testLoadUserByUsernameGrantedAuthorityMatchesRole() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.STANDARD.name())));
    }

    @Test
    void testLoadUserByUsernameGrantedAuthorityMatchesManagerRole() {
        Customer customer = new Customer();
        customer.setEmail("admin@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.MANAGER);

        when(customerRepository.getCustomerByEmail("admin@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("admin@example.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.MANAGER.name())));
    }

    @Test
    void testLoadUserByUsernameCallsCustomerService() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        userDetailsService.loadUserByUsername("test@example.com");

        verify(customerRepository, times(1)).getCustomerByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsernameGrantedAuthorityHasExactlyOneAuthority() {
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setHashedPassword("hashedPass");
        customer.setRole(Role.STANDARD);

        when(customerRepository.getCustomerByEmail("test@example.com")).thenReturn(Optional.of(customer));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    void testLoadUserByUsernameThrowsWhenCustomerNotFound() {
        when(customerRepository.getCustomerByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));
    }
}
