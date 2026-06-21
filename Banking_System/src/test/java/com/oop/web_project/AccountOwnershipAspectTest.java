package com.oop.web_project;

import com.oop.web_project.annotations.AccountOwnershipRequired;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.security.AccountOwnershipAspect;
import com.oop.web_project.services.CustomerService;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountOwnershipAspectTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AccountOwnershipAspect accountOwnershipAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private AccountOwnershipRequired annotation;

    @Test
    void testCheckAccountOwnershipFirstArgNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not-a-long"});

        assertThrows(IllegalArgumentException.class,
                () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
    }

    @Test
    void testCheckAccountOwnershipNullAuthenticationThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(null);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
        }
    }

    @Test
    void testCheckAccountOwnershipAnonymousAuthThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
        }
    }

    @Test
    void testCheckAccountOwnershipNotAuthenticatedThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
        }
    }

    @Test
    void testCheckAccountOwnershipCustomerDoesNotOwnAccountThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(customerService.customerOwnsAccountWithCard("test@example.com", 1L)).thenReturn(false);

            assertThrows(NotAccountOfCustomerException.class,
                    () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
        }
    }

    @Test
    void testCheckAccountOwnershipCustomerOwnsAccountProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(customerService.customerOwnsAccountWithCard("test@example.com", 1L)).thenReturn(true);

            assertDoesNotThrow(() -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
        }
    }

    @Test
    void testCheckAccountOwnershipCallsCustomerServiceWithCorrectArguments() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("owner@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(customerService.customerOwnsAccountWithCard("owner@example.com", 42L)).thenReturn(true);

            accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation);

            verify(customerService, times(1)).customerOwnsAccountWithCard("owner@example.com", 42L);
        }
    }

    @Test
    void testCheckAccountOwnershipFirstArgIntegerNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42});

        assertThrows(IllegalArgumentException.class,
                () -> accountOwnershipAspect.checkAccountOwnership(joinPoint, annotation));
    }
}
