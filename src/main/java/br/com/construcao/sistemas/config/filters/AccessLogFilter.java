package br.com.construcao.sistemas.config.filters;

import br.com.construcao.sistemas.model.AuditLog;
import br.com.construcao.sistemas.repository.AccessLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);
    public static final String ATTR_USER_EMAIL = "LOG_USER_EMAIL";

    private final AccessLogRepository repository;

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private String extractUserEmail(Object principal) {
        if (principal == null) return null;
        String principalStr = String.valueOf(principal);
        // Extract email from OAuth2 principal if it contains email pattern
        if (principalStr.contains("@")) {
            String[] parts = principalStr.split("[,\s]");
            for (String part : parts) {
                if (part.contains("@") && part.contains(".")) {
                    return part.trim();
                }
            }
        }
        return principalStr;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            chain.doFilter(req, res);
        } finally {
            AuditLog log = new AuditLog();
            log.setMethod(truncate(req.getMethod(), 255));
            log.setPath(truncate(req.getRequestURI(), 255));
            log.setStatusCode(res.getStatus());
            log.setIp(truncate(req.getRemoteAddr(), 255));

            // Primeiro tenta pegar email do OAuth2 (armazenado pelo CustomOAuth2Service)
            String userEmail = (String) req.getAttribute("OAUTH2_USER_EMAIL");
            System.out.println(userEmail);
            // Se não encontrou OAuth2, tenta pegar do JWT/Authentication normal
            if (userEmail == null) {
                Authentication a = SecurityContextHolder.getContext().getAuthentication();
                if (a != null && a.isAuthenticated()) {
                    userEmail = extractUserEmail(a.getPrincipal());
                }
            }
            
            if (userEmail != null) {
                log.setUserEmail(truncate(userEmail, 255));
                logger.info("{} {} {} {} - User: {}", req.getMethod(), req.getRequestURI(), log.getStatusCode(), log.getIp(), userEmail);
            } else {
                logger.info("{} {} {} {}", req.getMethod(), req.getRequestURI(), log.getStatusCode(), log.getIp());
            }
            
            repository.save(log);
        }
    }
}
