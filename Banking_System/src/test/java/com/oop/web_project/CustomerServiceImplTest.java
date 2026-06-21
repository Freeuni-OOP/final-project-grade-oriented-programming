package com.oop.web_project;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyActiveException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyDeactivatedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerNotFoundException;
import com.oop.web_project.persistence.AccountRepository;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");
        customer.setActive(false);
        customer.setAccounts(new ArrayList<>());
    }

    @Test
    void testRegisterCustomerNullCustomerThrowsException() {
        assertThrows(CustomerNotFoundException.class, () -> customerService.registerCustomer(null));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testRegisterCustomerValidCustomerSavesCustomer() {
        when(passwordEncoder.encode(customer.getHashedPassword())).thenReturn("");
        customerService.registerCustomer(customer);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void testGetCustomerByEmailFound() {
        when(customerRepository.getCustomerByEmail("john@example.com")).thenReturn(Optional.of(customer));
        Customer result = customerService.getCustomerByEmail("john@example.com");
        assertEquals(customer, result);
    }

    @Test
    void testGetCustomerByEmailNotFoundThrowsException() {
        when(customerRepository.getCustomerByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerByEmail("missing@example.com"));
    }

    @Test
    void testGetCustomerByIdFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Customer result = customerService.getCustomerById(1L);
        assertEquals(customer, result);
    }

    @Test
    void testGetCustomerByIdNotFoundThrowsException() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(2L));
    }

    @Test
    void testActivateCustomerNotFoundThrowsException() {
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.activateCustomer(1L));
    }

    @Test
    void testActivateCustomerAlreadyActiveThrowsException() {
        customer.setActive(true);
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.of(customer));
        assertThrows(CustomerAlreadyActiveException.class, () -> customerService.activateCustomer(1L));
    }

    @Test
    void testActivateCustomerInactiveActivatesCustomer() {
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.of(customer));
        customerService.activateCustomer(1L);
        assertTrue(customer.isActive());
    }

    @Test
    void testDeactivateCustomerNotFoundThrowsException() {
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.deactivateCustomer(1L));
    }

    @Test
    void testDeactivateCustomerAlreadyInactiveThrowsException() {
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.of(customer));
        assertThrows(CustomerAlreadyDeactivatedException.class, () -> customerService.deactivateCustomer(1L));
    }

    @Test
    void testDeactivateCustomerActiveDeactivatesCustomer() {
        customer.setActive(true);
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.of(customer));
        customerService.deactivateCustomer(1L);
        assertFalse(customer.isActive());
    }

    @Test
    void testDeleteCustomerNotFoundThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(1L));
    }

    @Test
    void testDeleteCustomerFoundDeletesCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        customerService.deleteCustomer(1L);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void testUpdateCustomerNotFoundThrowsException() {
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(1L, null, null, null, null));
    }

    @Test
    void testUpdateCustomerFoundUpdatesCustomer() {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setFirstName("Old");
        when(customerRepository.findWithLockById(1L)).thenReturn(Optional.of(existingCustomer));
        Customer updatedData = new Customer();
        updatedData.setId(99L);
        updatedData.setFirstName("New");
        customerService.updateCustomer(1L, updatedData.getFirstName(), updatedData.getLastName(), updatedData.getPhoneNumber(), updatedData.getAddress());
        assertEquals("New", existingCustomer.getFirstName());
        assertEquals(1L, existingCustomer.getId());
    }

    @Test
    void testGetCustomersByAccountCustomersFound() {
        List<Customer> customers = List.of(customer);
        when(customerRepository.getCustomersByAccounts_Id(1L)).thenReturn(customers);
        List<Customer> result = customerService.getCustomersByAccount(1L);
        assertEquals(customers, result);
    }

    @Test
    void testGetCustomersByAccountEmptyButAccountExistsReturnsEmptyList() {
        when(customerRepository.getCustomersByAccounts_Id(1L)).thenReturn(new ArrayList<>());
        when(accountRepository.existsById(1L)).thenReturn(true);
        List<Customer> result = customerService.getCustomersByAccount(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetCustomersByAccountEmptyAndAccountNotFoundThrowsException() {
        when(customerRepository.getCustomersByAccounts_Id(1L)).thenReturn(new ArrayList<>());
        when(accountRepository.existsById(1L)).thenReturn(false);
        assertThrows(AccountNotFoundException.class, () -> customerService.getCustomersByAccount(1L));
    }
}