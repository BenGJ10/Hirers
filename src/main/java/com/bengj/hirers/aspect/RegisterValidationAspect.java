package com.bengj.hirers.aspect;

import com.bengj.hirers.dto.RegisterRequestDto;
import com.bengj.hirers.entity.HirersUser;
import com.bengj.hirers.exception.RegistrationValidationException;
import com.bengj.hirers.repository.HirersUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterValidationAspect {

    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final HirersUserRepository hirersUserRepository;

    @Before("execution(* com.bengj.hirers.auth.AuthController.register(..))")
    /**
     * Validates the user registration request before proceeding with the registration process.
     * It checks for compromised passwords and existing users with the same email or mobile number.
     * If validation fails, it throws a RegistrationValidationException with the corresponding error messages.
     * 
     * @param joinPoint
     */
    public void validateBeforeRegister(JoinPoint joinPoint){

        Object[] args = joinPoint.getArgs();
        RegisterRequestDto registerRequestDto = (RegisterRequestDto) args[0];
        log.info("🔍 Validating user registration request");

        Map<String, String> errors = new HashMap<>();

        // 1. Check if the provided password is compromised using the CompromisedPasswordChecker
        CompromisedPasswordDecision decision = compromisedPasswordChecker.check(registerRequestDto.password());
        if (decision.isCompromised()) {
            errors.put("password", "Choose a strong password");
        }

        // 2. Check if a user with the same email or mobile number already exists in the database
        Optional<HirersUser> existingUser = hirersUserRepository.readUserByEmailOrMobileNumber(
                registerRequestDto.email(), registerRequestDto.mobileNumber());

        if(existingUser.isPresent()){
            HirersUser user = existingUser.get();
            if (user.getEmail().equalsIgnoreCase(registerRequestDto.email())) {
                errors.put("email", "Email is already registered");
            }
            if (user.getMobileNumber().equals(registerRequestDto.mobileNumber())) {
                errors.put("mobileNumber", "Mobile number is already registered");
            }
        }

        // 3. Stop execution if validation fails
        if(!errors.isEmpty()){
            log.warn("❌ Registration validation failed: {}", errors);
            throw new RegistrationValidationException(errors);
        }

        log.info("✅ Registration validation passed");
    }
}
