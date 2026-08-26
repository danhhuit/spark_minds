package com.sparkminds.library.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleAuthenticationFailureHandler
                implements AuthenticationFailureHandler {

        @Value("${app.frontend-url}")
        private String frontendUrl;

        @Override
        public void onAuthenticationFailure(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException exception) throws IOException {
                String baseUrl = frontendUrl.endsWith("/")
                                ? frontendUrl.substring(
                                                0,
                                                frontendUrl.length() - 1)
                                : frontendUrl;

                response.sendRedirect(
                                baseUrl
                                                + "/?oauthError=google_login_failed");
        }
}
