package com.oop.web_project;

import com.oop.web_project.exceptions.customerExceptions.CustomerCannotBeAuthenticatedException;
import com.oop.web_project.services.JWTServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceImplTest {

    @InjectMocks
    private JWTServiceImpl jwtService;

    private static final String SECRET_KEY =
            Base64.getEncoder().encodeToString("thisis32byteslongsecretkeyyouuse".getBytes());
    private static final Long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void testGenerateTokenReturnsNonNullToken() {
        String token = jwtService.generateToken("test@example.com");

        assertNotNull(token);
    }

    @Test
    void testGenerateTokenReturnsNonEmptyToken() {
        String token = jwtService.generateToken("test@example.com");

        assertFalse(token.isBlank());
    }

    @Test
    void testGenerateTokenReturnsDifferentTokensForDifferentEmails() {
        String token1 = jwtService.generateToken("user1@example.com");
        String token2 = jwtService.generateToken("user2@example.com");

        assertNotEquals(token1, token2);
    }

    @Test
    void testExtractEmailReturnsCorrectEmail() {
        String token = jwtService.generateToken("test@example.com");

        String email = jwtService.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void testExtractEmailInvalidTokenThrowsException() {
        assertThrows(CustomerCannotBeAuthenticatedException.class,
                () -> jwtService.extractEmail("invalid.jwt.token"));
    }

    @Test
    void testExtractEmailMalformedTokenThrowsException() {
        assertThrows(CustomerCannotBeAuthenticatedException.class,
                () -> jwtService.extractEmail("not-a-jwt-at-all"));
    }

    @Test
    void testValidateTokenValidTokenReturnsTrue() {
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        Boolean result = jwtService.validateToken(token, userDetails);

        assertTrue(result);
    }

    @Test
    void testValidateTokenWrongEmailReturnsFalse() {
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("other@example.com");

        Boolean result = jwtService.validateToken(token, userDetails);

        assertFalse(result);
    }

    @Test
    void testValidateTokenExpiredThrowsException() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        assertThrows(CustomerCannotBeAuthenticatedException.class, () -> jwtService.validateToken(token, userDetails));
    }

    @Test
    void testValidateTokenChecksUsernameAgainstToken() {
        String token = jwtService.generateToken("test@example.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@example.com");

        jwtService.validateToken(token, userDetails);

        verify(userDetails, atLeastOnce()).getUsername();
    }
}
