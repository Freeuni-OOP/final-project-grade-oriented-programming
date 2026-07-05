package com.oop.web_project;

import com.oop.web_project.config.SecurityConfig;
import com.oop.web_project.filters.JWTFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void testCorsAllowsLocalFrontendPorts() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JWTFilter.class));
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");
        CorsConfiguration config = source.getCorsConfiguration(request);

        // Vite may move to another port, so local dev should still work.
        assertEquals("http://localhost:5174", config.checkOrigin("http://localhost:5174"));
        assertEquals("http://127.0.0.1:5173", config.checkOrigin("http://127.0.0.1:5173"));
    }
}
