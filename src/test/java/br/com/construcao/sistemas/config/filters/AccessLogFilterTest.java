package br.com.construcao.sistemas.config.filters;

import br.com.construcao.sistemas.model.AuditLog;
import br.com.construcao.sistemas.repository.AccessLogRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class AccessLogFilterTest {

    private AccessLogFilter filter;
    private AccessLogRepository repository;

    @BeforeEach
    void setup() {
        repository = mock(AccessLogRepository.class);
        filter = new AccessLogFilter(repository);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegistrarLogComUsuarioAutenticado() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse res = new MockHttpServletResponse();

        SecurityContext context = new SecurityContextImpl();
        var auth = new UsernamePasswordAuthenticationToken(
                "user@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, res, chain);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository, times(1)).save(captor.capture());
        AuditLog log = captor.getValue();

        assertThat(log.getMethod()).isEqualTo("GET");
        assertThat(log.getPath()).isEqualTo("/api/test");
        assertThat(log.getStatusCode()).isEqualTo(200);
        assertThat(log.getIp()).isEqualTo(req.getRemoteAddr());
        assertThat(log.getUserEmail()).isEqualTo("user@email.com");
    }

    @Test
    void testRegistrarLogSemUsuarioQuandoNaoAutenticado() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, res, chain);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository, times(1)).save(captor.capture());
        AuditLog log = captor.getValue();

        assertThat(log.getMethod()).isEqualTo("POST");
        assertThat(log.getPath()).isEqualTo("/login");
        assertThat(log.getStatusCode()).isEqualTo(200);
        assertThat(log.getIp()).isEqualTo(req.getRemoteAddr());
        assertThat(log.getUserEmail()).isNull();
    }
}