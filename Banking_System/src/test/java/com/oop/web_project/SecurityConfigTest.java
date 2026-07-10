package com.oop.web_project;

import com.oop.web_project.config.SecurityConfig;
import com.oop.web_project.filters.JWTFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JWTFilter jwtFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtFilter);
    }

    @Test
    void testRoleHierarchyNotNull() {
        RoleHierarchy roleHierarchy = SecurityConfig.roleHierarchy();
        assertNotNull(roleHierarchy);
    }

    @Test
    void testMethodSecurityExpressionHandlerNotNull() {
        RoleHierarchy roleHierarchy = SecurityConfig.roleHierarchy();
        MethodSecurityExpressionHandler handler = securityConfig.methodSecurityExpressionHandler(roleHierarchy);
        assertNotNull(handler);
    }

    @Test
    void testAuthenticationManagerDelegatesToConfiguration() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);
        AuthenticationManager result = securityConfig.authenticationManager(authConfig);
        assertEquals(mockManager, result);
    }

    @Test
    void testCorsConfigurationSourceNotNull() {
        assertNotNull(securityConfig.corsConfigurationSource());
    }

    @Test
    void testCorsConfigurationAllowedMethodsContainsExpected() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedMethods().containsAll(List.of("GET", "POST", "DELETE", "OPTIONS", "PUT", "PATCH")));
    }

    @Test
    void testCorsConfigurationAllowCredentialsIsTrue() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void testCorsConfigurationAllowedOriginPatternsContainsLocalhost() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedOriginPatterns().contains("http://localhost:*"));
    }

    @Test
    void testCorsConfigurationAllowedOriginPatternsContains127001() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedOriginPatterns().contains("http://127.0.0.1:*"));
    }

    @Test
    void testCorsConfigurationAllowedHeadersContainsWildcard() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedHeaders().contains("*"));
    }
}