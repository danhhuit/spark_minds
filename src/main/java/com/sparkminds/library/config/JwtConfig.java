package com.sparkminds.library.config;

import com.sparkminds.library.security.jwt.RevokedTokenValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

        @Bean
        public SecretKey jwtSecretKey(JwtProperties properties) {
                try {
                        byte[] keyBytes = Base64.getDecoder()
                                        .decode(properties.secret().trim());

                        if (keyBytes.length < 32) {
                                throw new IllegalStateException(
                                                "JWT_SECRET must contain at least 32 bytes");
                        }

                        return new SecretKeySpec(
                                        keyBytes,
                                        "HmacSHA256");
                } catch (IllegalArgumentException exception) {
                        throw new IllegalStateException(
                                        "JWT_SECRET must be valid Base64",
                                        exception);
                }
        }

        @Bean
        public JwtEncoder jwtEncoder(SecretKey secretKey) {
                return NimbusJwtEncoder
                                .withSecretKey(secretKey)
                                .algorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtDecoder jwtDecoder(
                        SecretKey secretKey,
                        JwtProperties properties,
                        RevokedTokenValidator revokedTokenValidator) {
                NimbusJwtDecoder decoder = NimbusJwtDecoder
                                .withSecretKey(secretKey)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();

                var standardValidator = JwtValidators.createDefaultWithIssuer(
                                properties.issuer());

                decoder.setJwtValidator(
                                new DelegatingOAuth2TokenValidator<Jwt>(
                                                standardValidator,
                                                revokedTokenValidator));

                return decoder;
        }
}