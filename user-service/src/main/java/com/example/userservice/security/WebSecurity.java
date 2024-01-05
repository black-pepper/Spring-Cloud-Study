package com.example.userservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurity {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable());
        http.authorizeHttpRequests(request->{
            request.requestMatchers(antMatcher("/**")).permitAll();
            request.requestMatchers(antMatcher("/users/**")).permitAll();
            request.requestMatchers(antMatcher("/h2-console/**")).permitAll();
        });
        http.headers(headers->headers.frameOptions(frameOptions->frameOptions.disable()));
        return http.build();
    }
}