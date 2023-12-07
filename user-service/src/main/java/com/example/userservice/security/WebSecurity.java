package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment environment;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationManager authenticationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->{
                    request.requestMatchers(antMatcher("/actuator/**")).permitAll();
                    request.requestMatchers(antMatcher("/**")).permitAll();
                })
                .addFilter(getAuthenticationFilter(authenticationConfiguration));
//                .headers(header -> header.frameOptions(frameOptionsConfig -> frameOptionsConfig.disable()))
//                .apply(new MyCustomSecurity());
        http.headers().frameOptions().disable(); //H2 Console 설정

        return http.build();
    }

//    private AuthenticationFilter getAuthenticationFilter(HttpSecurity http) throws Exception {
//        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
//        AuthenticationFilter authenticationFilter =
//                new AuthenticationFilter(authenticationManager, userService, environment);
////        authenticationFilter.setAuthenticationManager(authenticationManager);
//        http.addFilter(authenticationFilter);
//        return authenticationFilter;
//    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, userService, environment);
        return authenticationFilter;
    }

    //@Bean
//    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }

//    @Bean
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
//    }

//    public class MyCustomSecurity extends AbstractHttpConfigurer<MyCustomSecurity, HttpSecurity> {
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//
//            AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
//            AuthenticationFilter authenticationFilter = new AuthenticationFilter();
//            authenticationFilter.setAuthenticationManager(authenticationManager);
//            http.addFilter(authenticationFilter);
//        }
//
//        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//            auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
//        }
//    }

}