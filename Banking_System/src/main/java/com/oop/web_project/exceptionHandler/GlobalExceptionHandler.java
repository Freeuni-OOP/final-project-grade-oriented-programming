package com.oop.web_project.exceptionHandler;

import com.oop.web_project.exceptions.accountExceptions.AccountAlreadyActiveException;
import com.oop.web_project.exceptions.accountExceptions.AccountAlreadyDeactivatedException;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.cardExceptions.*;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyActiveException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAlreadyDeactivatedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerCannotBeAuthenticatedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerNotFoundException;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyExchangeException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    // ACC
    @ExceptionHandler(AccountAlreadyActiveException.class)
    public ResponseEntity<String> handleAlreadyActiveAccount(AccountAlreadyActiveException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(AccountAlreadyDeactivatedException.class)
    public ResponseEntity<String> handleDeactivatedAccount(AccountAlreadyDeactivatedException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFound(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    //CARD
    @ExceptionHandler(CardAlreadyActiveException.class)
    public ResponseEntity<String> handleActiveCard(CardAlreadyActiveException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(SameCardTransferException.class)
    public ResponseEntity<String> handleSameCardTransfer(SameCardTransferException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(DuplicateCurrencyException.class)
    public ResponseEntity<String> handleDuplicateCurrency(DuplicateCurrencyException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(CardAlreadyDeactivatedException.class)
    public ResponseEntity<String> handleDeactivatedCard(CardAlreadyDeactivatedException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<String> handleAlreadyExistingCard(CardAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(CardBalanceNotFoundException.class)
    public ResponseEntity<String> handleBalanceNotFound(CardBalanceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<String> handleCardNotFound(CardNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(InsufficientMoneyOnCardException.class)
    public ResponseEntity<String> handleInsufficientMoney(InsufficientMoneyOnCardException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    public ResponseEntity<String> handleInvalidCurrency(InvalidCurrencyException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    //Customer
    @ExceptionHandler(CustomerAlreadyActiveException.class)
    public ResponseEntity<String> handleActiveCustomer(CustomerAlreadyActiveException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(CustomerAlreadyDeactivatedException.class)
    public ResponseEntity<String> handleDeactivatedCustomer(CustomerAlreadyDeactivatedException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> handleCustomerNotFound(CustomerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(CustomerCannotBeAuthenticatedException.class)
    public ResponseEntity<String> handleCustomerNotAuthenticated(CustomerCannotBeAuthenticatedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    //transaction
    @ExceptionHandler(CurrencyExchangeException.class)
    public ResponseEntity<String> handleCurrencyExchangeFailure(CurrencyExchangeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }


    //other

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Request violates data Integrity!");
    }
}
