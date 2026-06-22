package com.oop.web_project;

import com.oop.web_project.annotations.AccountAccessPermissionRequired;
import com.oop.web_project.annotations.CardAccessPermissionRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.entities.Role;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.cardExceptions.NotCardOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAccessDeniedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.security.AccessPermissionAspect;
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
class AccessPermissionAspectTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccessPermissionAspect accessPermissionAspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private CardAccessPermissionRequired cardAnnotation;

    @Mock
    private AccountAccessPermissionRequired accountAnnotation;

    @Mock
    private CustomerAccessPermissionRequired customerAnnotation;

    private SecurityContext mockAuthenticatedContext(String email) {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(context.getAuthentication()).thenReturn(auth);
        return context;
    }

    @Test
    void testCheckCardAccessPermissionFirstArgNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not-a-long"});

        assertThrows(IllegalArgumentException.class,
                () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
    }

    @Test
    void testCheckCardAccessPermissionFirstArgIntegerNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42});

        assertThrows(IllegalArgumentException.class,
                () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
    }

    @Test
    void testCheckCardAccessPermissionNullAuthenticationThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(null);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionAnonymousAuthThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionNotAuthenticatedThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionManagerBypassesOwnershipCheck() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));

            verify(customerRepository, never()).customerWithEmailOwnsCard(anyString(), anyLong());
        }
    }

    @Test
    void testCheckCardAccessPermissionCustomerDoesNotOwnCardThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.customerWithEmailOwnsCard("test@example.com", 1L)).thenReturn(false);

            assertThrows(NotCardOfCustomerException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionCustomerOwnsCardProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.customerWithEmailOwnsCard("test@example.com", 1L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionCallsRepositoryWithCorrectArguments() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("owner@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("owner@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.customerWithEmailOwnsCard("owner@example.com", 42L)).thenReturn(true);

            accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation);

            verify(customerRepository, times(1)).customerWithEmailOwnsCard("owner@example.com", 42L);
        }
    }

    @Test
    void testCheckAccountAccessPermissionFirstArgNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"not-a-long"});

        assertThrows(IllegalArgumentException.class,
                () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
    }

    @Test
    void testCheckAccountAccessPermissionFirstArgIntegerNotLongThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42});

        assertThrows(IllegalArgumentException.class,
                () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
    }

    @Test
    void testCheckAccountAccessPermissionNullAuthenticationThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(null);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionAnonymousAuthThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionNotAuthenticatedThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionManagerBypassesOwnershipCheck() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionAccountHasNoCustomersProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(1L)).thenReturn(false);
            when(customerRepository.existsByEmailAndAccountsId("test@example.com", 1L)).thenReturn(false);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionCustomerDoesNotOwnAccountThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(1L)).thenReturn(true);
            when(customerRepository.existsByEmailAndAccountsId("test@example.com", 1L)).thenReturn(false);

            assertThrows(NotAccountOfCustomerException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionCustomerOwnsAccountProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(1L)).thenReturn(true);
            when(customerRepository.existsByEmailAndAccountsId("test@example.com", 1L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionCallsRepositoryWithCorrectArguments() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("owner@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("owner@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(42L)).thenReturn(true);
            when(customerRepository.existsByEmailAndAccountsId("owner@example.com", 42L)).thenReturn(true);

            accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation);

            verify(customerRepository, times(1)).existsByEmailAndAccountsId("owner@example.com", 42L);
        }
    }

    @Test
    void testCheckCustomerAccessPermissionFirstArgNotLongOrStringThrowsIllegalArgumentException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionNullAuthenticationThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(null);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionAnonymousAuthThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken auth = mock(AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionNotAuthenticatedThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);

            assertThrows(CustomerIsNotAuthenticatedException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionManagerWithLongArgBypassesOwnershipCheck() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));

            verify(customerRepository, never()).existsByEmailAndId(anyString(), anyLong());
        }
    }

    @Test
    void testCheckCustomerAccessPermissionManagerWithStringArgBypassesOwnershipCheck() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"other@example.com"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionCustomerDoesNotOwnIdThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsByEmailAndId("test@example.com", 1L)).thenReturn(false);

            assertThrows(CustomerAccessDeniedException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionCustomerOwnsIdProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsByEmailAndId("test@example.com", 1L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionEmailArgMatchesAuthenticatedEmailProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test@example.com"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionEmailArgDoesNotMatchAuthenticatedEmailThrowsException() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"other@example.com"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(CustomerAccessDeniedException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionCallsRepositoryWithCorrectArguments() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("owner@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("owner@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsByEmailAndId("owner@example.com", 42L)).thenReturn(true);

            accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation);

            verify(customerRepository, times(1)).existsByEmailAndId("owner@example.com", 42L);
        }
    }
}