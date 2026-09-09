package com.sparkminds.library.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class TimeToLivePropertiesTest {

  @Test
  void everyLifetimeIsInterpretedInSeconds() {
    TimeToLiveProperties properties = new TimeToLiveProperties(11, 22, 33, 44, 55, 66, 77);

    assertEquals(Duration.ofSeconds(11), properties.accessToken());
    assertEquals(Duration.ofSeconds(22), properties.refreshToken());
    assertEquals(Duration.ofSeconds(33), properties.emailVerification());
    assertEquals(Duration.ofSeconds(44), properties.passwordReset());
    assertEquals(Duration.ofSeconds(55), properties.emailChangeVerification());
    assertEquals(Duration.ofSeconds(66), properties.socialLoginCode());
    assertEquals(Duration.ofSeconds(77), properties.borrowing());
  }
}
