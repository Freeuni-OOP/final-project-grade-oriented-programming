package com.oop.web_project;

import com.oop.web_project.exceptions.customerExceptions.CustomerCannotBeAuthenticatedException;
import com.oop.web_project.services.AuthServiceImpl;
import com.oop.web_project.services.JWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testAuthenticateCustomerAuthenticatedReturnsToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken("test@example.com")).thenReturn("jwt-token");

        String result = authService.authenticateCustomer("test@example.com", "password");

        assertEquals("jwt-token", result);
    }

    @Test
    void testAuthenticateCustomerNotAuthenticatedThrowsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        assertThrows(CustomerCannotBeAuthenticatedException.class,
                () -> authService.authenticateCustomer("test@example.com", "wrong"));
    }

    @Test
    void testAuthenticateCustomerCallsAuthenticationManager() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        authService.authenticateCustomer("test@example.com", "password");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testAuthenticateCustomerCallsJwtServiceWithEmail() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken("test@example.com")).thenReturn("jwt-token");

        authService.authenticateCustomer("test@example.com", "password");

        verify(jwtService, times(1)).generateToken("test@example.com");
    }

    @Test
    void testAuthenticateCustomerAuthenticationManagerThrowsExceptionPropagates() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class,
                () -> authService.authenticateCustomer("test@example.com", "wrong"));
    }

    @Test
    void testAuthenticateCustomerNotAuthenticatedDoesNotCallJwtService() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        assertThrows(CustomerCannotBeAuthenticatedException.class,
                () -> authService.authenticateCustomer("test@example.com", "wrong"));

        verifyNoInteractions(jwtService);
    }
}
