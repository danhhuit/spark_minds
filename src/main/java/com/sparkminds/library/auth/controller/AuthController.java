package com.sparkminds.library.auth.controller;

import com.sparkminds.library.auth.dto.request.RegisterRequest;
import com.sparkminds.library.auth.service.RegistrationService;
import com.sparkminds.library.common.api.MessageResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.sparkminds.library.auth.dto.request.ChangePasswordRequest;
import com.sparkminds.library.auth.dto.request.ForgotPasswordRequest;
import com.sparkminds.library.auth.dto.request.ResetPasswordRequest;
import com.sparkminds.library.auth.service.PasswordService;
import com.sparkminds.library.auth.dto.request.LoginRequest;
import com.sparkminds.library.auth.dto.request.RefreshTokenRequest;
import com.sparkminds.library.auth.dto.request.SocialLoginExchangeRequest;
import com.sparkminds.library.auth.dto.response.CurrentUserResponse;
import com.sparkminds.library.auth.dto.response.TokenResponse;
import com.sparkminds.library.auth.service.AuthService;
import com.sparkminds.library.auth.service.SocialLoginCodeService;
// import com.sparkminds.library.auth.service.PasswordService;
import com.sparkminds.library.auth.service.EmailChangeService;
import com.sparkminds.library.auth.dto.request.ChangeEmailRequest;
import com.sparkminds.library.auth.dto.request.VerifyEmailChangeRequest;
import com.sparkminds.library.auth.service.EmailChangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

        private final AuthService authService;
        private final RegistrationService registrationService;
        private final PasswordService passwordService;
        private final EmailChangeService emailChangeService;
        private final SocialLoginCodeService socialLoginCodeService;

        @PostMapping("/login")
        @Operation(summary = "Login and issue tokens")
        public ResponseEntity<TokenResponse> login(
                        @Valid @RequestBody LoginRequest request) {
                return ResponseEntity.ok(
                                authService.login(request));
        }

        @PostMapping("/register")
        @Operation(summary = "Register a new member")
        public ResponseEntity<MessageResponse> register(
                        @Valid @RequestBody RegisterRequest request) {
                return ResponseEntity.ok(
                                registrationService.register(request));
        }

        @PostMapping("/forgot-password")
        @Operation(summary = "Request password reset email")
        public ResponseEntity<MessageResponse> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request) {
                return ResponseEntity.ok(
                                passwordService.requestPasswordReset(
                                                request.email()));
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset password using email token")
        public ResponseEntity<MessageResponse> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                return ResponseEntity.ok(
                                passwordService.resetPassword(request));
        }

        @PostMapping("/change-password")
        @Operation(summary = "Change current password")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<MessageResponse> changePassword(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody ChangePasswordRequest request) {
                return ResponseEntity.ok(
                                passwordService.changePassword(jwt, request));
        }

        @GetMapping("/verify-email")
        @Operation(summary = "Verify member email")
        public ResponseEntity<MessageResponse> verifyEmail(
                        @RequestParam String token) {
                return ResponseEntity.ok(
                                registrationService.verifyEmail(token));
        }

        @PostMapping("/refresh")
        @Operation(summary = "Rotate refresh token")
        public ResponseEntity<TokenResponse> refresh(
                        @Valid @RequestBody RefreshTokenRequest request) {
                return ResponseEntity.ok(
                                authService.refresh(request.refreshToken()));
        }

        @PostMapping("/social/exchange")
        @Operation(summary = "Exchange one-time social login code")
        public ResponseEntity<TokenResponse> exchangeSocialLoginCode(
                        @Valid @RequestBody SocialLoginExchangeRequest request) {
                return ResponseEntity.ok(
                                socialLoginCodeService.exchange(
                                                request.code()));
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Void> logout(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody RefreshTokenRequest request) {
                authService.logout(
                                jwt,
                                request.refreshToken());

                return ResponseEntity.noContent().build();
        }

        @PostMapping("/change-email/request")
        @Operation(summary = "Send verification code to new email")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<MessageResponse> requestEmailChange(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody ChangeEmailRequest request) {
                return ResponseEntity.ok(
                                emailChangeService.requestEmailChange(
                                                jwt,
                                                request));
        }

        @PostMapping("/change-email/verify")
        @Operation(summary = "Verify code and change email")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<MessageResponse> verifyEmailChange(
                        @AuthenticationPrincipal Jwt jwt,
                        @Valid @RequestBody VerifyEmailChangeRequest request) {
                return ResponseEntity.ok(
                                emailChangeService.verifyEmailChange(
                                                jwt,
                                                request));
        }

        @GetMapping("/me")
        @Operation(summary = "Current authenticated user")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<CurrentUserResponse> me(
                        @AuthenticationPrincipal Jwt jwt) {
                return ResponseEntity.ok(
                                CurrentUserResponse.from(jwt));
        }
}
