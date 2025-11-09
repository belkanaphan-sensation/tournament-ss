package org.bn.sensation.core.common.logging;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class EndpointLoggingAspect {

    private static final int MAX_ARGUMENT_LENGTH = 30;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAroundRestControllers(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!log.isDebugEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = currentRequest();
        String httpMethod = request != null ? request.getMethod() : "N/A";
        String uri = request != null ? request.getRequestURI() : "N/A";
        String query = request != null ? request.getQueryString() : null;
        String qualifiedMethod = joinPoint.getSignature().toShortString();
        String arguments = formatArguments(joinPoint.getArgs());

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.debug(
                    "Handled {} {}{} -> {} with arguments [{}] in {} ms. Result: {}",
                    httpMethod,
                    uri,
                    query != null ? ("?" + query) : "",
                    qualifiedMethod,
                    arguments,
                    duration,
                    stringifyResult(result));
            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.debug(
                    "Failed {} {}{} -> {} with arguments [{}] after {} ms. Cause: {}",
                    httpMethod,
                    uri,
                    query != null ? ("?" + query) : "",
                    qualifiedMethod,
                    arguments,
                    duration,
                    throwable.getClass().getSimpleName());
            throw throwable;
        }
    }

    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        return Arrays.stream(args)
                .filter(this::isLoggableArgument)
                .map(this::stringifyArgument)
                .collect(Collectors.joining(", "));
    }

    private boolean isLoggableArgument(Object arg) {
        if (arg == null) {
            return true;
        }

        return !(arg instanceof HttpServletRequest)
                && !(arg instanceof HttpServletResponse)
                && !(arg instanceof BindingResult);
    }

    private String stringifyArgument(Object arg) {
        if (arg == null) {
            return "null";
        }
        String value = arg.toString();
        if (value.length() > MAX_ARGUMENT_LENGTH) {
            return value.substring(0, MAX_ARGUMENT_LENGTH) + "...";
        }
        return value;
    }

    private String stringifyResult(Object result) {
        if (result == null) {
            return "null";
        }
        String value = result.toString();
        if (value.length() > MAX_ARGUMENT_LENGTH) {
            return value.substring(0, MAX_ARGUMENT_LENGTH) + "...";
        }
        return value;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }
}

