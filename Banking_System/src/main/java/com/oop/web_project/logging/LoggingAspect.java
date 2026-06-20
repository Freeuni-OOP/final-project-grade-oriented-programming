package com.oop.web_project.logging;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_EXECUTION_THRESHOLD_MS = 1000;

    @Pointcut("execution(* com.oop.web_project.services.*.*(..))")
    public void serviceLayer() {}


    @Pointcut("execution(* com.oop.web_project.persistence.*.*(..))")
    public void persistenceLayer() {}


    @Pointcut("execution(* com.oop.web_project.restController.*.*(..))")
    public void restControllerLayer() {}



    @Around("restControllerLayer()")
    public Object logRestControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(attributes == null) {
            log.error("Something went wrong!");
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        String runtimeUri = request.getRequestURI();
        String httpMethod = request.getMethod();

        log.info("Request with uri {} has been received by API. HTTP method type {}", runtimeUri, httpMethod);

        Object result = null;

        try {
            result = joinPoint.proceed();
        } catch(Throwable throwable) {
            log.error("Request with uri {} and HTTP method type {} ran into problems!", runtimeUri, httpMethod);

            log.error("Exception {} was thrown with message: {}",
                    throwable.getClass(), throwable.getMessage());

            throw throwable;
        }

        HttpServletResponse httpServletResponse = attributes.getResponse();

        if(httpServletResponse == null) {
            log.error("Something went wrong!");
            return joinPoint.proceed();
        }

        String responseStatusCode = HttpStatus.valueOf(httpServletResponse.getStatus()).name();
        int status = httpServletResponse.getStatus();

        log.info("Request with uri {} and HTTP method type {} has been resolved with status {} {} ",
                runtimeUri, httpMethod, status, responseStatusCode);

        return result;
    }

    @Around("serviceLayer()")
    public Object logServiceExecution(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringTypeName();

        log.info("Service method {} of {} started execution", methodName, className);
        log.debug("Service method {} called with arguments: {}", methodName, pjp.getArgs());

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime >= SLOW_EXECUTION_THRESHOLD_MS) {
            log.warn("Service method {} of {} exceeded slow execution threshold: {} ms",
                    methodName, className, executionTime);
        } else {
            log.debug("Service method {} of {} completed in {} ms", methodName, className, executionTime);
        }

        log.info("Service method {} of {} finished successfully", methodName, className);
        return result;
    }

    @Around("persistenceLayer()")
    public Object logPersistenceExecution(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringTypeName();

        log.info("Repository method {} of {} started execution", methodName, className);
        log.debug("Repository method {} called with arguments: {}", methodName, pjp.getArgs());

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long executionTime = System.currentTimeMillis() - start;

        if (executionTime >= SLOW_EXECUTION_THRESHOLD_MS) {
            log.warn("Repository method {} of {} exceeded slow execution threshold: {} ms",
                    methodName, className, executionTime);
        } else {
            log.debug("Repository method {} of {} completed in {} ms", methodName, className, executionTime);
        }

        log.info("Repository method {} of {} finished successfully", methodName, className);
        return result;
    }

    @AfterThrowing(pointcut = "serviceLayer() || persistenceLayer()", throwing = "ex")
    public void logGlobalError(JoinPoint joinPoint, Exception ex) {
        log.error("Exception {} thrown in {} by method {} with message: {}",
                ex.getClass().getSimpleName(),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}