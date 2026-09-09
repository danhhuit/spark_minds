package com.sparkminds.library.auth.oauth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Replaces Spring Boot's Whitelabel 404 with a useful UI message when the optional Google OAuth
 * profile has not been configured.
 */
@Controller
@ConditionalOnProperty(
    name = "app.oauth2.google.enabled",
    havingValue = "false",
    matchIfMissing = true)
public class GoogleOAuthUnavailableController {

  @GetMapping("/oauth2/authorization/google")
  public String googleLoginUnavailable() {
    return "redirect:/?oauthError=google_not_configured";
  }
}
