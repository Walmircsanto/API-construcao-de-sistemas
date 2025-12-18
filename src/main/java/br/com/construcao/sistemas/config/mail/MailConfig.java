package br.com.construcao.sistemas.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host:}") String host,
            @Value("${spring.mail.port:0}") int port,
            @Value("${spring.mail.username:}") String user,
            @Value("${spring.mail.password:}") String pass
    ) {
        var sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        if (!host.isBlank()) sender.setHost(host);
        if (port > 0) sender.setPort(port);
        if (!user.isBlank()) sender.setUsername(user);
        if (!pass.isBlank()) sender.setPassword(pass);

        var props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", !user.isBlank());
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }
}
