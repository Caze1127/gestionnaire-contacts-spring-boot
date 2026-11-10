package com.appli2.accessingdatamysql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                .csrf(csrf ->
                        csrf.disable()
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/login.html",
                                "/register.html",

                                "/forgot-password.html",
                                "/reset-password.html",

                                "/scripts/login.js",
                                "/scripts/register.js",

                                "/scripts/forgot-password.js",
                                "/scripts/reset-password.js",

                                "/scripts/theme.js",

                                "/styles/login.css",
                                "/styles/register.css",
                                "/styles/style.css",

                                "/demo/login",
                                "/register",

                                "/forgot-password",
                                "/reset-password"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/admin.html",
                                "/scripts/admin.js",
                                "/admin/**",

                                "/users.html",
                                "/scripts/users.js",

                                "/demo/all",
                                "/demo/add",
                                "/demo/update/**",
                                "/demo/delete/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                "/",
                                "/index.html",

                                "/account.html",
                                "/scripts/account.js",
                                "/account/**",

                                "/contact.html",
                                "/contacts.html",

                                "/scripts/index.js",
                                "/scripts/auth.js",
                                "/scripts/contacts.js",
                                "/scripts/contact-details.js",

                                "/contacts/**",
                                "/profile"
                        )
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form

                        .loginPage(
                                "/login.html"
                        )

                        .loginProcessingUrl(
                                "/demo/login"
                        )

                        .usernameParameter(
                                "email"
                        )

                        .passwordParameter(
                                "password"
                        )

                        .successHandler(
                                (
                                        request,
                                        response,
                                        authentication
                                ) -> {

                                    response.setStatus(
                                            200
                                    );

                                    response.setContentType(
                                            "text/plain;charset=UTF-8"
                                    );

                                    response
                                            .getWriter()
                                            .write(
                                                    "Connexion réussie"
                                            );
                                }
                        )

                        .failureHandler(
                                (
                                        request,
                                        response,
                                        exception
                                ) -> {

                                    response.setStatus(
                                            401
                                    );

                                    response.setContentType(
                                            "text/plain;charset=UTF-8"
                                    );

                                    response
                                            .getWriter()
                                            .write(
                                                    "Email ou mot de passe incorrect"
                                            );
                                }
                        )
                )

                .logout(logout -> logout

                        .logoutUrl(
                                "/logout"
                        )

                        .logoutSuccessHandler(
                                (
                                        request,
                                        response,
                                        authentication
                                ) -> {

                                    response.setStatus(
                                            200
                                    );
                                }
                        )
                );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}