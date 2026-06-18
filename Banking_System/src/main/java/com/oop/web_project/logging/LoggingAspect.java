package com.oop.web_project.logging;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_EXECUTION_THRESHOLD_MS = 1000;

    @Pointcut("execution(* com.oop.web_project.services.*.*(..))")
    public void serviceLayer() {}

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
}