package dvoraka.avservice.rest.configuration

import dvoraka.avservice.rest.service.RestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

/**
 * Configuration spec.
 */
@ContextConfiguration(classes = SpringWebConfig.class)
@ActiveProfiles(['rest', 'rest-amqp', 'core', 'amqp', 'client', 'amqp-rest', 'db'])
@WebAppConfiguration
class RestAmqpConfigISpec extends Specification {

    @Autowired
    RestService avRestService


    def "test"() {
        expect:
            true
    }
}
