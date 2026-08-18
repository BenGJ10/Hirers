package com.bengj.hirers.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ExceptionAuditAspect {

    @AfterThrowing(pointcut = "execution(* com.bengj.hirers..*.*(..))", throwing = "ex")
    /**
     * Logs exceptions thrown by methods in the com.bengj.hirers package.
     *
     * @param joinPoint the join point representing the method execution
     * @param ex        the exception thrown by the method
     */
    public void logAfterException(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] methodArgs = joinPoint.getArgs();

        log.error("❌ Exception occurred in method: {}", methodName);
        log.error("📥 Arguments: {}", Arrays.toString(methodArgs));
        log.error("💥 Exception type: {}", ex.getClass().getSimpleName());
        log.error("🧾 Exception message: {}", ex.getMessage());
    }
}
