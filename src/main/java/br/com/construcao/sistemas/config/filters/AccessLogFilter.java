package br.com.construcao.sistemas.config.filters;

import br.com.construcao.sistemas.model.AuditLog;
import br.com.construcao.sistemas.repository.AccessLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    public static final String ATTR_USER_EMAIL = "LOG_USER_EMAIL";

    private final AccessLogRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            chain.doFilter(req, res);
        } finally {
            AuditLog log = new AuditLog();
            log.setMethod(req.getMethod());
            log.setPath(req.getRequestURI());
            log.setStatusCode(res.getStatus());
            log.setIp(req.getRemoteAddr());

            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.isAuthenticated()) log.setUserEmail(String.valueOf(a.getPrincipal()));

            repository.save(log);
        }
    }
}
