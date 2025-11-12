package br.com.construcao.sistemas.config.cors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CorsProperties {

    @Value("${CORS_ALLOWED_ORIGINS}")
    private String allowedOrigins;

    @Value("${CORS_ALLOWED_METHODS}")
    private String allowedMethods;

    @Value("${CORS_ALLOWED_HEADERS}")
    private String allowedHeaders;

    @Value("${CORS_ALLOW_CREDENTIALS:true}")
    private boolean allowCredentials;

    @Value("${CORS_MAX_AGE:3600}")
    private long maxAge;

    public List<String> getAllowedOrigins() {
        return Arrays.asList(allowedOrigins.split(","));
    }

    public List<String> getAllowedMethods() {
        return Arrays.asList(allowedMethods.split(","));
    }

    public List<String> getAllowedHeaders() {
        return Arrays.asList(allowedHeaders.split(","));
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }
}
