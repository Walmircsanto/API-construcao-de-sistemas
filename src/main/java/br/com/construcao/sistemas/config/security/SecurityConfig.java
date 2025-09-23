package br.com.construcao.sistemas.config.security;

import br.com.construcao.sistemas.config.AccessLogFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AccessLogFilter accessLogFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, AccessLogFilter accessLogFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.accessLogFilter = accessLogFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwt) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/nexus/auth/**").permitAll()
                        .requestMatchers("/api/nexus/user/**").permitAll()
                        .requestMatchers("/api/nexus/suspects/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(accessLogFilter, JwtAuthFilter.class);
        return http.build();
    }
}
