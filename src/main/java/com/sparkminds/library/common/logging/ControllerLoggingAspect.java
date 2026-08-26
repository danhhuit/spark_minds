package com.sparkminds.library.common.logging;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Log4j2
public class ControllerLoggingAspect {

    @Around("within(com.sparkminds.library..controller..*)")
    public Object logControllerMethod(
            ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = getCurrentRequest();

        String httpMethod = request == null
                ? "N/A"
                : request.getMethod();

        String requestUri = request == null
                ? "N/A"
                : request.getRequestURI();

        String handler = joinPoint.getSignature().toShortString();

        String username = getCurrentUsername();

        String arguments = summarizeArguments(
                joinPoint.getArgs(),
                requestUri);

        long startTime = System.nanoTime();

        log.info(
                "FUNCTION_CALL method={} path={} user={} handler={} args={}",
                httpMethod,
                requestUri,
                username,
                handler,
                arguments);

        try {
            Object result = joinPoint.proceed();

            long durationMillis = (System.nanoTime() - startTime)
                    / 1_000_000;

            log.info(
                    "FUNCTION_RETURN method={} path={} user={} handler={} durationMs={} result={}",
                    httpMethod,
                    requestUri,
                    username,
                    handler,
                    durationMillis,
                    summarizeResult(result));

            return result;
        } catch (Throwable exception) {
            long durationMillis = (System.nanoTime() - startTime)
                    / 1_000_000;

            log.error(
                    "FUNCTION_ERROR method={} path={} user={} handler={} durationMs={} exception={} message={}",
                    httpMethod,
                    requestUri,
                    username,
                    handler,
                    durationMillis,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception);

            throw exception;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }

        return null;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        return authentication.getName();
    }

    private String summarizeArguments(
            Object[] arguments,
            String requestUri) {
        if (requestUri.startsWith("/api/auth")) {
            return "[authentication-arguments-masked]";
        }

        return Arrays.stream(arguments)
                .map(this::summarizeArgument)
                .collect(Collectors.joining(
                        ", ",
                        "[",
                        "]"));
    }

    private String summarizeArgument(Object argument) {
        if (argument == null) {
            return "null";
        }

        if (argument instanceof Jwt jwt) {
            return "Jwt{sub=" + jwt.getSubject() + "}";
        }

        if (argument instanceof MultipartFile file) {
            return "MultipartFile{name="
                    + file.getName()
                    + ", originalFilename="
                    + file.getOriginalFilename()
                    + ", size="
                    + file.getSize()
                    + "}";
        }

        if (argument instanceof ServletRequest
                || argument instanceof ServletResponse) {
            return argument.getClass().getSimpleName();
        }

        if (argument instanceof Number
                || argument instanceof Boolean
                || argument instanceof Enum<?>) {
            return String.valueOf(argument);
        }

        /*
         * Không gọi toString() trên request DTO vì DTO có thể
         * chứa password, email, token hoặc thông tin cá nhân.
         */
        return argument.getClass().getSimpleName()
                + "{fields=masked}";
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "void";
        }

        if (result instanceof ResponseEntity<?> response) {
            Object body = response.getBody();

            return "ResponseEntity{status="
                    + response.getStatusCode().value()
                    + ", bodyType="
                    + (body == null
                            ? "empty"
                            : body.getClass().getSimpleName())
                    + "}";
        }

        return result.getClass().getSimpleName()
                + "{fields=masked}";
    }
}