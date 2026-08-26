package com.sparkminds.library.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
public class HttpLoggingFilter
                extends OncePerRequestFilter {

        @Override
        protected boolean shouldNotFilter(
                        HttpServletRequest request) {
                return !request.getRequestURI()
                                .startsWith("/api/");
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                String requestId = UUID.randomUUID().toString();
                long startTime = System.nanoTime();

                ThreadContext.put("requestId", requestId);

                log.info(
                                "HTTP_REQUEST method={} path={} remoteAddress={}",
                                request.getMethod(),
                                request.getRequestURI(),
                                request.getRemoteAddr());

                try {
                        filterChain.doFilter(request, response);
                } finally {
                        long durationMillis = (System.nanoTime() - startTime)
                                        / 1_000_000;

                        log.info(
                                        "HTTP_RESPONSE method={} path={} status={} durationMs={}",
                                        request.getMethod(),
                                        request.getRequestURI(),
                                        response.getStatus(),
                                        durationMillis);

                        ThreadContext.remove("requestId");
                }
        }
}