package com.sparkminds.library.auth.oauth;

import com.sparkminds.library.auth.entity.OAuthIdentity;
import com.sparkminds.library.auth.repository.OAuthIdentityRepository;
import com.sparkminds.library.auth.service.SocialLoginCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class GoogleAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private static final String PROVIDER = "GOOGLE";

    private final OAuthIdentityRepository identityRepository;
    private final SocialLoginCodeService loginCodeService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal()
                instanceof OidcUser oidcUser)) {
            throw new ServletException(
                    "Google authentication principal is invalid"
            );
        }

        OAuthIdentity identity = identityRepository
                .findByProviderAndProviderSubject(
                        PROVIDER,
                        oidcUser.getSubject()
                )
                .orElseThrow(() ->
                        new ServletException(
                                "Google identity was not persisted"
                        )
                );

        String rawCode =
                loginCodeService.issueFor(identity.getUser());

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                frontendBaseUrl()
                        + "/?socialCode="
                        + URLEncoder.encode(
                                rawCode,
                                StandardCharsets.UTF_8
                        )
        );
    }

    private String frontendBaseUrl() {
        return frontendUrl.endsWith("/")
                ? frontendUrl.substring(
                        0,
                        frontendUrl.length() - 1
                )
                : frontendUrl;
    }
}
