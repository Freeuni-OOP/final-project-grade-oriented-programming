
package com.oop.web_project;

import com.oop.web_project.logging.LoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @InjectMocks
    private LoggingAspect loggingAspect;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("save");
        when(signature.getDeclaringTypeName()).thenReturn("com.oop.web_project.services.UserService");
        when(pjp.getArgs()).thenReturn(new Object[]{"arg1", 42});
    }

    @Test
    void testLogServiceExecutionReturnsResultFromProceed() throws Throwable {
        Object expected = "service-result";
        when(pjp.proceed()).thenReturn(expected);

        Object actual = loggingAspect.logServiceExecution(pjp);

        assertEquals(expected, actual);
    }

    @Test
    void testLogServiceExecutionCallsProceedExactlyOnce() throws Throwable {
        when(pjp.proceed()).thenReturn(new Object());

        loggingAspect.logServiceExecution(pjp);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogServiceExecutionWithNullReturnValue() throws Throwable {
        when(pjp.proceed()).thenReturn(null);

        Object actual = loggingAspect.logServiceExecution(pjp);

        assertNull(actual);
    }

    @Test
    void testLogServiceExecutionReadsMethodNameFromSignature() throws Throwable {
        when(pjp.proceed()).thenReturn(new Object());

        loggingAspect.logServiceExecution(pjp);

        verify(signature, atLeastOnce()).getName();
    }

    @Test
    void testLogServiceExecutionReadsClassNameFromSignature() throws Throwable {
        when(pjp.proceed()).thenReturn(new Object());

        loggingAspect.logServiceExecution(pjp);

        verify(signature, atLeastOnce()).getDeclaringTypeName();
    }

    @Test
    void testLogServiceExecutionReadsArgsFromJoinPoint() throws Throwable {
        when(pjp.proceed()).thenReturn(new Object());

        loggingAspect.logServiceExecution(pjp);

        verify(pjp, atLeastOnce()).getArgs();
    }

    @Test
    void testLogServiceExecutionWithEmptyArgs() throws Throwable {
        when(pjp.getArgs()).thenReturn(new Object[]{});
        when(pjp.proceed()).thenReturn("result");

        Object actual = loggingAspect.logServiceExecution(pjp);

        assertNotNull(actual);
    }

}
