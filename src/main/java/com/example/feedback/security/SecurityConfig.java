package com.example.feedback.security;

import com.example.feedback.entity.ActivityType;
import com.example.feedback.service.UserActivityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final UserActivityService activityService;

    public SecurityConfig(CustomUserDetailsService userDetailsService, UserActivityService activityService) {
        this.userDetailsService = userDetailsService;
        this.activityService = activityService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ════════════════════════════════════════════════════════════════════════
    // 1. DEDICATED ADMIN SECURITY FILTER CHAIN (/admin/**)
    // ════════════════════════════════════════════════════════════════════════
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(adminAuthenticationSuccessHandler())
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null) {
                        activityService.logActivityByEmail(authentication.getName(), ActivityType.LOGOUT, "Admin logged out");
                    }
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> response.sendRedirect("/admin/login"))
                .accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect("/admin/login?denied=true"))
            );

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. FACULTY / CLIENT SECURITY FILTER CHAIN (Default requests)
    // ════════════════════════════════════════════════════════════════════════
    @Bean
    @Order(2)
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/register", "/sample-csv").permitAll()
                .requestMatchers("/client/**").hasRole("CLIENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(clientAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null) {
                        activityService.logActivityByEmail(authentication.getName(), ActivityType.LOGOUT, "User logged out");
                    }
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> response.sendRedirect("/login"))
                .accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect("/login?denied=true"))
            );

        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    // Success Handler for Admin Login (/admin/login)
    @Bean
    public AuthenticationSuccessHandler adminAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                activityService.logActivityByEmail(authentication.getName(), ActivityType.LOGIN, "Admin logged into Admin Portal");
                response.sendRedirect("/admin/dashboard");
            } else {
                // Non-admin user attempted to authenticate via Admin Portal login
                request.getSession().invalidate();
                response.sendRedirect("/admin/login?error=unauthorized");
            }
        };
    }

    // Success Handler for Client Login (/login)
    @Bean
    public AuthenticationSuccessHandler clientAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                activityService.logActivityByEmail(authentication.getName(), ActivityType.LOGIN, "Admin logged in via client login");
                response.sendRedirect("/admin/dashboard");
            } else if (roles.contains("ROLE_CLIENT")) {
                activityService.logActivityByEmail(authentication.getName(), ActivityType.LOGIN, "Faculty logged into Faculty Portal");
                response.sendRedirect("/client/dashboard");
            } else {
                response.sendRedirect("/login");
            }
        };
    }
}
