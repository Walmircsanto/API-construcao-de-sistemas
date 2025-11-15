package br.com.construcao.sistemas.config.security;

import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserRepository userRepository;
    private JwtAuthFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        filter = new JwtAuthFilter(jwtService, userRepository);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testDeveAutenticarUsuarioQuandoTokenValido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");

        // Simular Claims retornando id do usuário
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(jwtService.parse("token-valido")).thenReturn(claims);

        // Simular usuário encontrado
        User user = new User();
        user.setId(1L);
        user.setEmail("usuario@test.com");
        user.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, chain);

        // Verificar autenticação configurada corretamente
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("usuario@test.com", auth.getPrincipal());
        assertEquals(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), auth.getAuthorities());

        // Verificar se o chain continuou
        verify(chain).doFilter(request, response);
    }

    @Test
    void testDeveIgnorarQuandoTokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtService.parse("token-invalido")).thenThrow(new RuntimeException("Token inválido"));

        filter.doFilterInternal(request, response, chain);

        // Nenhuma autenticação deve ser configurada
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void testDeveIgnorarQuandoHeaderAusente() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void testDeveIgnorarQuandoHeaderNaoComecaComBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Token abc123");

        filter.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}