package dvoraka.avservice.server.configuration.amqp

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * Configuration test.
 */
@ContextConfiguration(classes = [AmqpConfig.class])
@ActiveProfiles(["amqp", "no-db"])
class AmqpConfigISpec extends Specification {

    @Autowired
    RabbitTemplate rabbitTemplate


    def "test"() {
        expect:
            true
    }
}