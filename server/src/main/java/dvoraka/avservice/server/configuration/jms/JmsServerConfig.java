package dvoraka.avservice.server.configuration.jms;

import dvoraka.avservice.MessageProcessor;
import dvoraka.avservice.client.ServerComponent;
import dvoraka.avservice.client.jms.JmsComponent;
import dvoraka.avservice.db.service.MessageInfoService;
import dvoraka.avservice.server.AvServer;
import dvoraka.avservice.server.BasicAvServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

/**
 * JMS server configuration for the import.
 */
@Configuration
@Profile("jms")
public class JmsServerConfig {

    @Value("${avservice.jms.receiveTimeout:2000}")
    private long receiveTimeout;

    @Value("${avservice.jms.resultDestination:result}")
    private String resultDestination;
    @Value("${avservice.jms.checkDestination:check}")
    private String checkDestination;

    @Value("${avservice.serviceId:default1}")
    private String serviceId;


    @Bean
    public AvServer fileServer(
            ServerComponent fileServerComponent,
            MessageProcessor messageProcessor,
            MessageInfoService messageInfoService
    ) {
        return new BasicAvServer(
                serviceId,
                fileServerComponent,
                messageProcessor,
                messageInfoService
        );
    }

    @Bean
    public ServerComponent fileServerComponent(
            JmsTemplate fileServerJmsTemplate,
            MessageInfoService messageInfoService
    ) {
        return new JmsComponent(
                resultDestination, serviceId, fileServerJmsTemplate, messageInfoService);
    }

    @Bean
    public MessageConverter fileServerMessageConverter() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTypeIdPropertyName("typeId");

        return messageConverter;
    }

    @Bean
    public JmsTemplate fileServerJmsTemplate(
            ConnectionFactory serverConnectionFactory,
            MessageConverter fileServerMessageConverter
    ) {
        JmsTemplate template = new JmsTemplate(serverConnectionFactory);
        template.setReceiveTimeout(receiveTimeout);
        template.setMessageConverter(fileServerMessageConverter);

        return template;
    }

    @Bean
    public MessageListener fileServerMessageListener(ServerComponent fileServerComponent) {
        return fileServerComponent;
    }

    @Bean
    public SimpleMessageListenerContainer fileServerMessageListenerContainer(
            ConnectionFactory serverConnectionFactory,
            MessageListener fileServerMessageListener
    ) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(serverConnectionFactory);
        container.setDestinationName(checkDestination);
        container.setMessageListener(fileServerMessageListener);

        return container;
    }
}
