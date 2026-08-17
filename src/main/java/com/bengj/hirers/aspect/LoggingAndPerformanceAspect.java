package com.bengj.hirers.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAndPerformanceAspect {

    @Around("execution(* com.bengj.hirers..*.*(..))")
    /**
     * Logs the method execution details and measures the execution time of the method.
     * It logs the method name, input arguments, and execution time in milliseconds.
     * 
     * @param joinPoint
     * @return The result of the method execution.
     * @throws Throwable if the method execution throws any exception.
     */
    public Object logAndMeasureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        Object[] methodArgs = joinPoint.getArgs();
        log.info("➡️ Entering method: {}", methodName);
        log.info("📥 Arguments: {}", Arrays.toString(methodArgs));

        Object result = joinPoint.proceed(methodArgs);
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("✅ Method executed successfully: {}", methodName);
        log.info("⏰ Execution time: {} ms", executionTime);

        return result;
    }
}
