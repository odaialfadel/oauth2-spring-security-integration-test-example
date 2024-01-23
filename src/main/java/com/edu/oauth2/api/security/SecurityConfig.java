package com.edu.oauth2.api.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(name = "authentication.active", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain authenticatedFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                        request -> request
                                .requestMatchers("/api/**")
                                .authenticated()
                                .anyRequest()
                                .permitAll())
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "authentication.active", havingValue = "false")
    public SecurityFilterChain unauthenticatedFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                request -> request
                        .anyRequest()
                        .permitAll());
        return http.build();
    }
}
