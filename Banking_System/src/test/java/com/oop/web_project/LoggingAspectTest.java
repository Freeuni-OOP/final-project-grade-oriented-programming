package com.oop.web_project;

import com.oop.web_project.aspect.logging.LoggingAspect;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private Signature signature;

    @InjectMocks
    private LoggingAspect aspect;

    @Test
    void testLogRestControllerExecutionAttributesNullProceedsDirectly() throws Throwable {
        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::getRequestAttributes).thenReturn(null);
            when(pjp.proceed()).thenReturn("result");

            Object result = aspect.logRestControllerExecution(pjp);

            assertEquals("result", result);
            verify(pjp).proceed();
        }
    }

    @Test
    void testLogRestControllerExecutionProceedSuccessReturnsResult() throws Throwable {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(attributes.getRequest()).thenReturn(request);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(pjp.proceed()).thenReturn("ok");
        when(attributes.getResponse()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

            Object result = aspect.logRestControllerExecution(pjp);

            assertEquals("ok", result);
        }
    }

    @Test
    void testLogRestControllerExecutionProceedThrowsRethrows() throws Throwable {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(attributes.getRequest()).thenReturn(request);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("POST");
        when(pjp.proceed()).thenThrow(new RuntimeException("boom"));

        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

            assertThrows(RuntimeException.class, () -> aspect.logRestControllerExecution(pjp));
        }
    }

    @Test
    void testLogRestControllerExecutionResponseNullProceedsAgain() throws Throwable {
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(attributes.getRequest()).thenReturn(request);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        when(pjp.proceed()).thenReturn("first").thenReturn("second");
        when(attributes.getResponse()).thenReturn(null);

        try (MockedStatic<RequestContextHolder> holder = mockStatic(RequestContextHolder.class)) {
            holder.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

            Object result = aspect.logRestControllerExecution(pjp);

            assertEquals("second", result);
            verify(pjp, times(2)).proceed();
        }
    }

    @Test
    void testLogServiceExecutionProceedReturnsResult() throws Throwable {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("findAll");
        when(signature.getDeclaringTypeName()).thenReturn("app.services.MemberService");
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenReturn("data");

        Object result = aspect.logServiceExecution(pjp);

        assertEquals("data", result);
    }

    @Test
    void testLogServiceExecutionProceedThrowsRethrows() throws Throwable {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("findAll");
        when(signature.getDeclaringTypeName()).thenReturn("app.services.MemberService");
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> aspect.logServiceExecution(pjp));
    }

    @Test
    void testLogPersistenceExecutionProceedReturnsResult() throws Throwable {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("save");
        when(signature.getDeclaringTypeName()).thenReturn("app.persistence.MemberRepository");
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenReturn("saved");

        Object result = aspect.logPersistenceExecution(pjp);

        assertEquals("saved", result);
    }

    @Test
    void testLogPersistenceExecutionProceedThrowsRethrows() throws Throwable {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("save");
        when(signature.getDeclaringTypeName()).thenReturn("app.persistence.MemberRepository");
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenThrow(new RuntimeException("constraint violation"));

        assertThrows(RuntimeException.class, () -> aspect.logPersistenceExecution(pjp));
    }

    @Test
    void testServiceLayerPointcutDoesNotThrow() {
        aspect.serviceLayer();
    }

    @Test
    void testPersistenceLayerPointcutDoesNotThrow() {
        aspect.persistenceLayer();
    }

    @Test
    void testRestControllerLayerPointcutDoesNotThrow() {
        aspect.restControllerLayer();
    }

    @Test
    void testLogGlobalErrorLogsExceptionDetails() {
        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature sig = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(sig);
        when(sig.getDeclaringTypeName()).thenReturn("app.services.MemberService");
        when(sig.getName()).thenReturn("findById");

        Exception ex = new IllegalArgumentException("not found");

        aspect.logGlobalError(joinPoint, ex);

        verify(joinPoint, times(2)).getSignature();
    }
}