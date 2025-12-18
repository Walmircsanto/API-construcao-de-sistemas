package br.com.construcao.sistemas.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            // Assumindo que o username é o ID do usuário
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
}