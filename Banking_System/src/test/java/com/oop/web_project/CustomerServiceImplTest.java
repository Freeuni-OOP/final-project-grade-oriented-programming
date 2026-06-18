package com.oop.web_project;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.persistence.AccountRepository;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        assertThrows(IllegalArgumentException.class, () -> customerService.registerCustomer(null));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void testRegisterCustomerValidCustomerSavesCustomer() {
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
        assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerByEmail("missing@example.com"));
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
        assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerById(2L));
    }

    @Test
    void testActivateCustomerNotFoundThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> customerService.activateCustomer(1L));
    }

    @Test
    void testActivateCustomerAlreadyActiveThrowsException() {
        customer.setActive(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        assertThrows(IllegalArgumentException.class, () -> customerService.activateCustomer(1L));
    }

    @Test
    void testActivateCustomerInactiveActivatesCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        customerService.activateCustomer(1L);
        assertTrue(customer.isActive());
    }

    @Test
    void testDeactivateCustomerNotFoundThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> customerService.deactivateCustomer(1L));
    }

    @Test
    void testDeactivateCustomerAlreadyInactiveThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        assertThrows(IllegalArgumentException.class, () -> customerService.deactivateCustomer(1L));
    }

    @Test
    void testDeactivateCustomerActiveDeactivatesCustomer() {
        customer.setActive(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        customerService.deactivateCustomer(1L);
        assertFalse(customer.isActive());
    }

    @Test
    void testDeleteCustomerNotFoundThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> customerService.deleteCustomer(1L));
    }

    @Test
    void testDeleteCustomerFoundDeletesCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        customerService.deleteCustomer(1L);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void testUpdateCustomerNotFoundThrowsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> customerService.updateCustomer(1L, customer));
    }

    @Test
    void testUpdateCustomerFoundUpdatesCustomer() {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setFirstName("Old");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        Customer updatedData = new Customer();
        updatedData.setId(99L);
        updatedData.setFirstName("New");
        customerService.updateCustomer(1L, updatedData);
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
        assertThrows(IllegalArgumentException.class, () -> customerService.getCustomersByAccount(1L));
    }
}
