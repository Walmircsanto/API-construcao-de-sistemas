package br.com.construcao.sistemas.config.security;

import br.com.construcao.sistemas.config.cors.CorsProperties;
import br.com.construcao.sistemas.config.filters.AccessLogFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AccessLogFilter accessLogFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          AccessLogFilter accessLogFilter,
                          CorsProperties corsProperties) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.accessLogFilter = accessLogFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/nexus/auth/**").permitAll()
                        .requestMatchers("/api/nexus/user/**").permitAll()
                        .requestMatchers("/api/nexus/emergency-contacts/**").permitAll()

                        .requestMatchers("/api/nexus/suspects/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(accessLogFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(corsProperties.getAllowedOrigins());
        cfg.setAllowedMethods(corsProperties.getAllowedMethods());
        cfg.setAllowedHeaders(corsProperties.getAllowedHeaders());
        cfg.setAllowCredentials(corsProperties.isAllowCredentials());
        cfg.setMaxAge(Duration.ofSeconds(corsProperties.getMaxAge()));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
