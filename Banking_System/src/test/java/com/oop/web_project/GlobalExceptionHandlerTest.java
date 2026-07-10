package com.oop.web_project;

import com.oop.web_project.exceptions.accountExceptions.*;
import com.oop.web_project.exceptions.cardExceptions.*;
import com.oop.web_project.exceptions.customerExceptions.*;
import com.oop.web_project.exceptions.otherExceptions.CouldNotExtractIdException;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyExchangeException;
import com.oop.web_project.exceptionHandler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAlreadyActiveAccountReturnsNotAcceptable() {
        AccountAlreadyActiveException ex = new AccountAlreadyActiveException("account is already active!");
        ResponseEntity<String> response = handler.handleAlreadyActiveAccount(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("account is already active!", response.getBody());
    }

    @Test
    void testHandleDeactivatedAccountReturnsNotAcceptable() {
        AccountAlreadyDeactivatedException ex = new AccountAlreadyDeactivatedException("account is already inactive!");
        ResponseEntity<String> response = handler.handleDeactivatedAccount(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("account is already inactive!", response.getBody());
    }

    @Test
    void testHandleAccountNotFoundReturnsNotFound() {
        AccountNotFoundException ex = new AccountNotFoundException("account not found!");
        ResponseEntity<String> response = handler.handleAccountNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("account not found!", response.getBody());
    }

    @Test
    void testHandleAccountNotBeingOfCustomerReturnsForbidden() {
        NotAccountOfCustomerException ex = new NotAccountOfCustomerException("not account of customer!");
        ResponseEntity<String> response = handler.handleAccountNotBeingOfCustomer(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("not account of customer!", response.getBody());
    }

    @Test
    void testHandleAccountNotActiveReturnsForbidden() {
        AccountIsNotActiveException ex = new AccountIsNotActiveException("account is not active!");
        ResponseEntity<String> response = handler.handleAccountNotActive(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("account is not active!", response.getBody());
    }

    @Test
    void testHandleActiveCardReturnsNotAcceptable() {
        CardAlreadyActiveException ex = new CardAlreadyActiveException("card is already active!");
        ResponseEntity<String> response = handler.handleActiveCard(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("card is already active!", response.getBody());
    }

    @Test
    void testHandleSameCardTransferReturnsNotAcceptable() {
        SameCardTransferException ex = new SameCardTransferException("same card transfer!");
        ResponseEntity<String> response = handler.handleSameCardTransfer(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("same card transfer!", response.getBody());
    }

    @Test
    void testHandleDuplicateCurrencyReturnsNotAcceptable() {
        DuplicateCurrencyException ex = new DuplicateCurrencyException("duplicate currency!");
        ResponseEntity<String> response = handler.handleDuplicateCurrency(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("duplicate currency!", response.getBody());
    }

    @Test
    void testHandleDeactivatedCardReturnsNotAcceptable() {
        CardAlreadyDeactivatedException ex = new CardAlreadyDeactivatedException("card is already inactive!");
        ResponseEntity<String> response = handler.handleDeactivatedCard(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("card is already inactive!", response.getBody());
    }

    @Test
    void testHandleAlreadyExistingCardReturnsConflict() {
        CardAlreadyExistsException ex = new CardAlreadyExistsException("card already exists!");
        ResponseEntity<String> response = handler.handleAlreadyExistingCard(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("card already exists!", response.getBody());
    }

    @Test
    void testHandleBalanceNotFoundReturnsNotFound() {
        CardBalanceNotFoundException ex = new CardBalanceNotFoundException("balance not found!");
        ResponseEntity<String> response = handler.handleBalanceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("balance not found!", response.getBody());
    }

    @Test
    void testHandleCardNotFoundReturnsNotFound() {
        CardNotFoundException ex = new CardNotFoundException("card not found!");
        ResponseEntity<String> response = handler.handleCardNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("card not found!", response.getBody());
    }

    @Test
    void testHandleInsufficientMoneyReturnsBadRequest() {
        InsufficientMoneyOnCardException ex = new InsufficientMoneyOnCardException("insufficient funds!");
        ResponseEntity<String> response = handler.handleInsufficientMoney(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("insufficient funds!", response.getBody());
    }

    @Test
    void testHandleInvalidCurrencyReturnsNotAcceptable() {
        InvalidCurrencyException ex = new InvalidCurrencyException("invalid currency!");
        ResponseEntity<String> response = handler.handleInvalidCurrency(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("invalid currency!", response.getBody());
    }

    @Test
    void testHandleCardNotOwnedByCustomerReturnsForbidden() {
        NotCardOfCustomerException ex = new NotCardOfCustomerException("not card of customer!");
        ResponseEntity<String> response = handler.handleCardNotOwnedByCustomer(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("not card of customer!", response.getBody());
    }

    @Test
    void testHandleCardNotActiveReturnsForbidden() {
        CardIsNotActiveException ex = new CardIsNotActiveException("card is not active!");
        ResponseEntity<String> response = handler.handleCardNotActive(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("card is not active!", response.getBody());
    }

    @Test
    void testHandleActiveCustomerReturnsNotAcceptable() {
        CustomerAlreadyActiveException ex = new CustomerAlreadyActiveException("customer is already active!");
        ResponseEntity<String> response = handler.handleActiveCustomer(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("customer is already active!", response.getBody());
    }

    @Test
    void testHandleDeactivatedCustomerReturnsNotAcceptable() {
        CustomerAlreadyDeactivatedException ex = new CustomerAlreadyDeactivatedException("customer is already inactive!");
        ResponseEntity<String> response = handler.handleDeactivatedCustomer(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("customer is already inactive!", response.getBody());
    }

    @Test
    void testHandleCustomerNotFoundReturnsNotFound() {
        CustomerNotFoundException ex = new CustomerNotFoundException("customer not found!");
        ResponseEntity<String> response = handler.handleCustomerNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("customer not found!", response.getBody());
    }

    @Test
    void testHandleCustomerNotAuthenticatedReturnsForbidden() {
        CustomerCannotBeAuthenticatedException ex = new CustomerCannotBeAuthenticatedException("cannot authenticate customer!");
        ResponseEntity<String> response = handler.handleCustomerNotAuthenticated(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("cannot authenticate customer!", response.getBody());
    }

    @Test
    void testHandleAlreadyRegisteredCustomerReturnsConflict() {
        CustomerAlreadyRegisteredException ex = new CustomerAlreadyRegisteredException("customer already registered!");
        ResponseEntity<String> response = handler.handleAlreadyRegisteredCustomer(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("customer already registered!", response.getBody());
    }

    @Test
    void testHandleCustomerDetailsNotFoundReturnsNotFound() {
        CustomerDetailsNotFoundException ex = new CustomerDetailsNotFoundException("customer details not found!");
        ResponseEntity<String> response = handler.handleCustomerDetailsNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("customer details not found!", response.getBody());
    }

    @Test
    void testHandleCustomerIsNotAuthenticatedReturnsForbidden() {
        CustomerIsNotAuthenticatedException ex = new CustomerIsNotAuthenticatedException("customer is not authenticated!");
        ResponseEntity<String> response = handler.handleCustomerIsNotAuthenticated(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("customer is not authenticated!", response.getBody());
    }

    @Test
    void testHandleCustomerAccessProhibitedReturnsForbidden() {
        CustomerAccessDeniedException ex = new CustomerAccessDeniedException("access denied!");
        ResponseEntity<String> response = handler.handleCustomerAccessProhibited(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("access denied!", response.getBody());
    }

    @Test
    void testHandleCustomerNotActiveReturnsForbidden() {
        CustomerIsNotActiveException ex = new CustomerIsNotActiveException("customer is not active!");
        ResponseEntity<String> response = handler.handleCustomerNotActive(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("customer is not active!", response.getBody());
    }

    @Test
    void testHandleCurrencyExchangeFailureReturnsBadRequest() {
        CurrencyExchangeException ex = new CurrencyExchangeException("currency exchange failed!");
        ResponseEntity<String> response = handler.handleCurrencyExchangeFailure(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("currency exchange failed!", response.getBody());
    }

    @Test
    void testHandleDataIntegrityViolationReturnsConflict() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation");
        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Request violates data Integrity!", response.getBody());
    }

    @Test
    void testHandleDataIntegrityViolationIgnoresOriginalMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("some internal db error");
        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);
        assertEquals("Request violates data Integrity!", response.getBody());
    }

    @Test
    void testHandleAuthFailureReturnsUnauthorized() {
        AuthenticationException ex = mock(AuthenticationException.class);
        ResponseEntity<String> response = handler.handleAuthFailure(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody());
    }

    @Test
    void testHandleIdExtractionFailureReturnsUnauthorized() {
        CouldNotExtractIdException ex = new CouldNotExtractIdException("could not extract id!");
        ResponseEntity<String> response = handler.handleIdExtractionFailure(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Identification is not present!", response.getBody());
    }

    @Test
    void testHandleValidationFailureReturnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        ResponseEntity<Map<String, String>> response = handler.handleValidationFailure(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", response.getBody().get("email"));
    }

    @Test
    void testHandleValidationFailureMultipleFieldsReturnsAllErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError emailError = new FieldError("object", "email", "must not be blank");
        FieldError nameError = new FieldError("object", "firstName", "size must be between 2 and 50");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(emailError, nameError));
        ResponseEntity<Map<String, String>> response = handler.handleValidationFailure(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be blank", response.getBody().get("email"));
        assertEquals("size must be between 2 and 50", response.getBody().get("firstName"));
    }

    @Test
    void testHandleValidationFailureDuplicateFieldKeepsFirstError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError first = new FieldError("object", "email", "must not be blank");
        FieldError second = new FieldError("object", "email", "must be a valid email");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(first, second));
        ResponseEntity<Map<String, String>> response = handler.handleValidationFailure(ex);
        assertEquals("must not be blank", response.getBody().get("email"));
    }
}