package br.com.construcao.sistemas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;
    @Value("${app.url.portal}")
    private String portalUrl;

    public void sendProvisionalPassword(String to, String name, String tempPassword, Instant expiresAt) {
        String subject = "Sua senha provisória";
        String expStr = expiresAt != null ? DateTimeFormatter.ISO_INSTANT.format(expiresAt) : null;

        StringBuilder body = new StringBuilder()
                .append("Olá, ").append(name).append("!\n\n")
                .append("Sua conta foi criada e uma senha provisória foi gerada:\n")
                .append("Senha provisória: ").append(tempPassword).append("\n\n")
                .append("Acesse: ").append(portalUrl).append("\n");

        if (expStr != null) body.append("Validade da senha provisória: ").append(expStr).append("\n");

        body.append("\nRecomendamos alterar sua senha no primeiro acesso.");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body.toString());

        mailSender.send(msg);
    }
}
