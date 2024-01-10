package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

    private final UserService userService;
    private final Environment env;
    private final AuthenticationManager authenticationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable());
        http.authorizeHttpRequests(request->{
            request.requestMatchers(antMatcher("/**")).permitAll();
            request.requestMatchers(antMatcher("/users/**")).permitAll();
            request.requestMatchers(antMatcher("/h2-console/**")).permitAll();
        }).addFilter(getAuthenticationFilter());
        http.headers(headers->headers.frameOptions(frameOptions->frameOptions.disable()));
        return http.build();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, userService, env);
        return authenticationFilter;
    }
}