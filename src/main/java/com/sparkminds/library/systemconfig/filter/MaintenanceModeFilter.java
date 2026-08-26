package com.sparkminds.library.systemconfig.filter;

import tools.jackson.databind.ObjectMapper;
import com.sparkminds.library.common.api.ApiErrorResponse;
import com.sparkminds.library.systemconfig.service.SystemConfigService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
public class MaintenanceModeFilter
                extends OncePerRequestFilter {

        private final SystemConfigService systemConfigService;
        private final ObjectMapper objectMapper;

        @Override
        protected boolean shouldNotFilter(
                        HttpServletRequest request) {
                String path = request.getRequestURI();

                if (!path.startsWith("/api/")) {
                        return true;
                }

                if (path.equals("/api/auth/login")
                                || path.equals(
                                                "/api/auth/social/exchange")) {
                        return true;
                }

                return path.startsWith(
                                "/api/admin/system-config");
        }

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                if (!systemConfigService.isMaintenanceMode()) {
                        filterChain.doFilter(request, response);
                        return;
                }

                ApiErrorResponse errorResponse = new ApiErrorResponse(
                                Instant.now(),
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE
                                                .getReasonPhrase(),
                                systemConfigService
                                                .getMaintenanceMessage(),
                                request.getRequestURI(),
                                Map.of());

                response.setStatus(
                                HttpStatus.SERVICE_UNAVAILABLE.value());

                response.setCharacterEncoding(
                                StandardCharsets.UTF_8.name());

                response.setContentType(
                                MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(
                                response.getWriter(),
                                errorResponse);
        }
}
