package dvoraka.avservice.rest.configuration;

import dvoraka.avservice.rest.service.RemoteRestService;
import dvoraka.avservice.rest.service.RestService;
import dvoraka.avservice.server.ServerComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * REST AMQP configuration.
 */
@Configuration
@Profile("rest-amqp")
public class RestAmqpConfig {

    @Bean
    public RestService avRestService(ServerComponent serverComponent) {
        return new RemoteRestService(serverComponent);
    }
}
