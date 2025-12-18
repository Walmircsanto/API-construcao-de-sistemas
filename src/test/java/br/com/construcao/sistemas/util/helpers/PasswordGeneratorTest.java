package br.com.construcao.sistemas.util.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void deveGerarSenhaComTamanhoCorreto() {
        int len = 12;
        String password = generator.generate(len);

        assertNotNull(password);
        assertEquals(len, password.length());
    }

    @Test
    void deveGerarSenhaNaoNula() {
        String password = generator.generate(10);
        assertNotNull(password);
    }

    @Test
    void deveGerarSomenteCaracteresPermitidos() {
        String allowed = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";

        String password = generator.generate(50);

        for (char c : password.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0,
                    "Caractere inválido encontrado: " + c);
        }
    }

    @Test
    void duasChamadasDevemGerarSenhasDiferentes() {
        String p1 = generator.generate(20);
        String p2 = generator.generate(20);

        assertNotEquals(p1, p2);
    }

    @Test
    void deveGerarSenhaComTamanhoMinimo() {
        String p = generator.generate(1);
        assertEquals(1, p.length());
    }

    @Test
    void deveGerarSenhaComTamanhoMaior() {
        String p = generator.generate(100);
        assertEquals(100, p.length());
    }
}