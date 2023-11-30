package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    //private final Environment environment;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->{
                    request.requestMatchers(antMatcher("/actuator/**")).permitAll();
                    request.requestMatchers(antMatcher("/**")).permitAll();})
                            .headers(header -> header.frameOptions(
                                frameOptionsConfig -> frameOptionsConfig.disable()));
                //.apply(new MyCustomSecurity());

        return http.build();
    }

//    public class MyCustomSecurity extends AbstractHttpConfigurer<MyCustomSecurity, HttpSecurity> {
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//
//            AuthenticationManager authenticationManager = http.getSharedObject(
//                    AuthenticationManager.class);
//            AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, userService, environment);
//            http.addFilter(authenticationFilter);
//        }
//
//        protected void configure2(AuthenticationManagerBuilder auth) throws Exception {
//            auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
//        }
//
//    }

}