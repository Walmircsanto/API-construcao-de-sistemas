package br.com.construcao.sistemas.util.helpers;

import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUserResolverTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthUserResolver resolver = new AuthUserResolver(userRepository);

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarIdDoUsuarioAutenticado() {
        User user = new User();
        user.setId(123L);
        user.setEmail("teste@teste.com");

        when(userRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(user));

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("teste@teste.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Long result = resolver.currentUserId();

        assertEquals(123L, result);
    }

    @Test
    void deveLancarExcecaoQuandoNaoAutenticado() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(context);

        assertThrows(UnauthorizedException.class, resolver::currentUserId);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("inexistente@teste.com");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        when(userRepository.findByEmail("inexistente@teste.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, resolver::currentUserId);
    }
}