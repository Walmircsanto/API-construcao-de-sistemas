package br.com.construcao.sistemas.config.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class RabbitConfig {

    @Value("${notifications.queues.exchange}")
    private String exchangeName;

    @Value("${notifications.queues.user}")
    private String userQueueName;

    @Value("${notifications.queues.topic}")
    private String topicQueueName;

    @Value("${notifications.queues.user-dlq}")
    private String userDlqName;

    @Value("${notifications.queues.topic-dlq}")
    private String topicDlqName;

    @Bean
    public TopicExchange notifExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue userQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName);
        args.put("x-dead-letter-routing-key", userDlqName);
        args.put("x-message-ttl", 30000);
        return new Queue(userQueueName, true, false, false, args);
    }

    @Bean
    public Queue topicQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName);
        args.put("x-dead-letter-routing-key", topicDlqName);
        return new Queue(topicQueueName, true, false, false, args);
    }

    @Bean
    public Queue userDlq() {
        return new Queue(userDlqName, true);
    }

    @Bean
    public Queue topicDlq() {
        return new Queue(topicDlqName, true);
    }

    @Bean
    public Binding bindUser(TopicExchange ex) {
        return BindingBuilder.bind(userQueue()).to(ex).with(userQueueName);
    }

    @Bean
    public Binding bindTopic(TopicExchange ex) {
        return BindingBuilder.bind(topicQueue()).to(ex).with(topicQueueName);
    }

    @Bean
    public Binding bindUserDlq(TopicExchange ex) {
        return BindingBuilder.bind(userDlq()).to(ex).with(userDlqName);
    }

    @Bean
    public Binding bindTopicDlq(TopicExchange ex) {
        return BindingBuilder.bind(topicDlq()).to(ex).with(topicDlqName);
    }

    @Bean
    public MessageConverter jacksonConverter(ObjectMapper om) {
        var conv = new Jackson2JsonMessageConverter(om);
        var mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTrustedPackages("*");
        conv.setJavaTypeMapper(mapper);
        return conv;
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter conv) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(conv);
        tpl.setExchange(exchangeName);
        tpl.setMandatory(true);
        tpl.setReturnsCallback(ret ->
                log.error("Returned message: replyCode={}, replyText={}, exchange={}, routingKey={}",
                        ret.getReplyCode(), ret.getReplyText(), ret.getExchange(), ret.getRoutingKey()));
        return tpl;
    }

}
