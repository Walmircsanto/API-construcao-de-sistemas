package br.com.construcao.sistemas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setup() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);

        setField(emailService, "from", "noreply@test.com");
        setField(emailService, "portalUrl", "https://google.com");
    }

    private void setField(Object target, String field, String value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ignored) {

        }
    }

    @Test
    void testSendProvisionalPassword_ComExpiracao() {
        String to = "user@mail.com";
        String name = "Diego";
        String tempPass = "ABC123!";
        Instant exp = Instant.parse("2025-01-20T00:00:00Z");

        emailService.sendProvisionalPassword(to, name, tempPass, exp);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();
        assertEquals("noreply@test.com", msg.getFrom());
        assertEquals("Sua senha provisória", msg.getSubject());
        assertArrayEquals(new String[]{to}, msg.getTo());

        ZonedDateTime zdt = exp.atZone(ZoneId.systemDefault());
        String expectedDate = zdt.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        assertTrue(msg.getText().contains("Olá, Diego!"));
        assertTrue(msg.getText().contains("Senha provisória: ABC123!"));
        assertTrue(msg.getText().contains("Acesse: https://google.com"));
        assertTrue(msg.getText().contains("Validade da senha provisória: " + expectedDate));
    }

    @Test
    void testSendProvisionalPassword_SemExpiracao() {
        String to = "user@mail.com";
        String name = "Diego";
        String tempPass = "XYZ789!";

        emailService.sendProvisionalPassword(to, name, tempPass, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();

        assertFalse(msg.getText().contains("Validade da senha provisória:"));
    }


    @Test
    void testSendPasswordReset_ComExpiracao() {
        String to = "user@mail.com";
        String name = "Diego";
        String link = "https://reset.com";
        Instant exp = Instant.parse("2025-05-01T10:30:00Z");

        emailService.sendPasswordReset(to, name, link, exp);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();

        assertEquals("Redefinição de senha", msg.getSubject());
        assertArrayEquals(new String[]{to}, msg.getTo());

        String expected = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(exp);

        assertTrue(msg.getText().contains("Recebemos uma solicitação para redefinir sua senha"));
        assertTrue(msg.getText().contains(link));
        assertTrue(msg.getText().contains("Este link expira em: " + expected));
    }

    @Test
    void testSendPasswordReset_SemExpiracao() {
        String to = "user@mail.com";
        String name = "Diego";
        String link = "https://reset.com";

        emailService.sendPasswordReset(to, name, link, null);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage msg = captor.getValue();

        assertFalse(msg.getText().contains("Este link expira em:"));
    }
}