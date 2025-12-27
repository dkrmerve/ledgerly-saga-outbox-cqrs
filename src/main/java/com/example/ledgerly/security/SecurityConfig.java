package com.example.ledgerly.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.*;

// AWS note (design-only):
// In production, jwtHmacSecret should come from AWS Secrets Manager/SSM,
// and JWT verification would typically use an external IdP (Cognito/Keycloak/Auth0).

@Configuration
public class SecurityConfig {

    @Value("${ledgerly.security.jwtHmacSecret}")
    private String jwtHmacSecret;

    @Bean
    @ConditionalOnProperty(name = "ledgerly.security.enabled", havingValue = "true", matchIfMissing = true)
    JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(jwtHmacSecret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    @ConditionalOnProperty(name = "ledgerly.security.enabled", havingValue = "true", matchIfMissing = true)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/api/stocks/**").hasAuthority("SCOPE_admin")
                .antMatchers("/api/orders/**").hasAuthority("SCOPE_orders.write")
                .antMatchers("/api/query/**").hasAuthority("SCOPE_orders.read")
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer().jwt();

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "ledgerly.security.enabled", havingValue = "false")
    SecurityFilterChain permissiveChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().anyRequest().permitAll();
        return http.build();
    }
}