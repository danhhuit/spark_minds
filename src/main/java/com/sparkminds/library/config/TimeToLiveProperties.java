package com.sparkminds.library.config;

import jakarta.validation.constraints.Min;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Centralizes every application lifetime in seconds. */
@ConfigurationProperties(prefix = "app.time-to-live")
@Validated
public record TimeToLiveProperties(
    @Min(1) long accessTokenSeconds,
    @Min(1) long refreshTokenSeconds,
    @Min(1) long emailVerificationSeconds,
    @Min(1) long passwordResetSeconds,
    @Min(1) long emailChangeVerificationSeconds,
    @Min(1) long socialLoginCodeSeconds,
    @Min(1) long borrowingSeconds) {

  public Duration accessToken() {
    return Duration.ofSeconds(accessTokenSeconds);
  }

  public Duration refreshToken() {
    return Duration.ofSeconds(refreshTokenSeconds);
  }

  public Duration emailVerification() {
    return Duration.ofSeconds(emailVerificationSeconds);
  }

  public Duration passwordReset() {
    return Duration.ofSeconds(passwordResetSeconds);
  }

  public Duration emailChangeVerification() {
    return Duration.ofSeconds(emailChangeVerificationSeconds);
  }

  public Duration socialLoginCode() {
    return Duration.ofSeconds(socialLoginCodeSeconds);
  }

  public Duration borrowing() {
    return Duration.ofSeconds(borrowingSeconds);
  }
}
