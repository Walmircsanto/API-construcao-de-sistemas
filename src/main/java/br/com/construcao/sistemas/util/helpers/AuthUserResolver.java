package br.com.construcao.sistemas.util.helpers;

import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserResolver {
    private final UserRepository users;

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new UnauthorizedException("Não autenticado");
        String email = auth.getName();
        return users.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}
