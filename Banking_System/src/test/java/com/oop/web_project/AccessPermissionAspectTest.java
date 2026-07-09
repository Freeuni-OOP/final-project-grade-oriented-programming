package com.oop.web_project;

import com.oop.web_project.annotations.AccountAccessPermissionRequired;
import com.oop.web_project.annotations.CardAccessPermissionRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.aspect.security.AccessPermissionAspect;
import com.oop.web_project.entities.Role;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.cardExceptions.NotCardOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAccessDeniedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.exceptions.otherExceptions.CouldNotExtractIdException;
import com.oop.web_project.persistence.CustomerRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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
    private MethodSignature methodSignature;

    @Mock
    private CardAccessPermissionRequired cardAnnotation;

    @Mock
    private AccountAccessPermissionRequired accountAnnotation;

    @Mock
    private CustomerAccessPermissionRequired customerAnnotation;


    private void setupJoinPointForExtractId(String paramName, Object argValue) {
        when(joinPoint.getArgs()).thenReturn(new Object[]{argValue});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{paramName});
    }

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
    void testCheckCardAccessPermissionArgIsNotLongThrowsIllegalArgumentException() {
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        setupJoinPointForExtractId("cardId", "not-a-long");

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionArgIsIntegerNotLongThrowsIllegalArgumentException() {
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        setupJoinPointForExtractId("cardId", 42);

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));
        }
    }

    @Test
    void testCheckCardAccessPermissionNullAuthenticationThrowsException() {

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
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        setupJoinPointForExtractId("cardId", 1L);

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
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        setupJoinPointForExtractId("cardId", 1L);

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
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        setupJoinPointForExtractId("cardId", 42L);

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
    void testCheckCardAccessPermissionResolvesCorrectArgWhenMultipleParamsPresent() {
        when(cardAnnotation.idArgName()).thenReturn("cardId");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"someString", 99L});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"email", "cardId"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.customerWithEmailOwnsCard("test@example.com", 99L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCardAccessPermission(joinPoint, cardAnnotation));

            verify(customerRepository, times(1)).customerWithEmailOwnsCard("test@example.com", 99L);
        }
    }


    @Test
    void testCheckAccountAccessPermissionArgIsNotLongThrowsIllegalArgumentException() {
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", "not-a-long");

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionArgIsIntegerNotLongThrowsIllegalArgumentException() {
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", 42);

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionNullAuthenticationThrowsException() {

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

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));

            verify(customerRepository, never()).existsCustomerByAccounts_Id(anyLong());
            verify(customerRepository, never()).existsByEmailAndAccountsId(anyString(), anyLong());
        }
    }

    @Test
    void testCheckAccountAccessPermissionAccountHasNoCustomersProceeds() {
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", 1L);

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(1L)).thenReturn(false);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));
        }
    }

    @Test
    void testCheckAccountAccessPermissionCustomerDoesNotOwnAccountThrowsException() {
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", 1L);

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
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", 1L);

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
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        setupJoinPointForExtractId("accountId", 42L);

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
    void testCheckAccountAccessPermissionResolvesCorrectArgWhenMultipleParamsPresent() {
        when(accountAnnotation.idArgName()).thenReturn("accountId");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"someString", 77L});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"email", "accountId"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsCustomerByAccounts_Id(77L)).thenReturn(true);
            when(customerRepository.existsByEmailAndAccountsId("test@example.com", 77L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkAccountAccessPermission(joinPoint, accountAnnotation));

            verify(customerRepository, times(1)).existsByEmailAndAccountsId("test@example.com", 77L);
        }
    }

    @Test
    void testCheckCustomerAccessPermissionNullAuthenticationThrowsException() {

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
    void testCheckCustomerAccessPermissionEmailArgMatchesAuthenticatedEmailProceeds() {
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test@example.com"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));

            verify(customerRepository, never()).existsByEmailAndId(anyString(), anyLong());
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
    void testCheckCustomerAccessPermissionManagerWithStringArgBypassesEmailCheck() {

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionManagerWithLongArgBypassesOwnershipCheck() {

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("manager@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("manager@example.com", Role.MANAGER)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));

            verify(customerRepository, never()).existsByEmailAndId(anyString(), anyLong());
        }
    }

    @Test
    void testCheckCustomerAccessPermissionArgIsIntegerNotLongThrowsIllegalArgumentException() {
        when(customerAnnotation.idArgName()).thenReturn("customerId");
        setupJoinPointForExtractId("customerId", 42);

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionParamNameNotFoundThrowsCouldNotExtractIdException() {
        when(customerAnnotation.idArgName()).thenReturn("customerId");
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"otherId"});

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);

            assertThrows(CouldNotExtractIdException.class,
                    () -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionCustomerDoesNotOwnIdThrowsException() {
        when(customerAnnotation.idArgName()).thenReturn("customerId");
        setupJoinPointForExtractId("customerId", 1L);

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
        when(customerAnnotation.idArgName()).thenReturn("customerId");
        setupJoinPointForExtractId("customerId", 1L);

        try (MockedStatic<SecurityContextHolder> holder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mockAuthenticatedContext("test@example.com");
            holder.when(SecurityContextHolder::getContext).thenReturn(context);
            when(customerRepository.existsByEmailAndRole("test@example.com", Role.MANAGER)).thenReturn(false);
            when(customerRepository.existsByEmailAndId("test@example.com", 1L)).thenReturn(true);

            assertDoesNotThrow(() -> accessPermissionAspect.checkCustomerAccessPermission(joinPoint, customerAnnotation));
        }
    }

    @Test
    void testCheckCustomerAccessPermissionCallsRepositoryWithCorrectArguments() {
        when(customerAnnotation.idArgName()).thenReturn("customerId");
        setupJoinPointForExtractId("customerId", 42L);

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