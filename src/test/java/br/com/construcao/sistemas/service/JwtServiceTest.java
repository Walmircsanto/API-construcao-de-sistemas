package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User dummyUser;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setEmail("teste@example.com");
        dummyUser.setRole(Role.ADMIN);
    }

    @Test
    void testDeveGerarAccessToken_ComJwtValido() {
        String token = jwtService.generateAccess(dummyUser);

        assertNotNull(token);

        Claims claims = jwtService.parse(token);

        assertEquals("1", claims.getSubject());
        assertEquals("teste@example.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(900_000L, diff, 2000);
    }

    @Test
    void testDeveGerarRefreshToken_ComJwtValido() {
        String token = jwtService.generateRefresh(dummyUser);

        Claims claims = jwtService.parse(token);

        assertEquals("1", claims.getSubject());
        assertEquals("teste@example.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));

        long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(2_592_000_000L, diff, 2000);
    }

    @Test
    void testDeveParsearTokenERetornarClaimsCorretos() {
        String token = jwtService.generateAccess(dummyUser);

        Claims claims = jwtService.parse(token);

        assertEquals("1", claims.getSubject());
        assertEquals("teste@example.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void testDeveLancarExcecaoParaTokenInvalido() {
        String invalidToken = "TOKEN.INVALIDO";

        assertThrows(Exception.class, () -> jwtService.parse(invalidToken));
    }

    @Test
    void testDeveExpirarTokenCorretamente() throws InterruptedException {
        Instant now = Instant.now();
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("1")
                .setExpiration(Date.from(now.plusMillis(50)))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                        "troque-por-uma-chave-grande-e-secreta".getBytes(StandardCharsets.UTF_8))
                .compact();

        assertThrows(Exception.class, () -> jwtService.parse(token));
    }
}