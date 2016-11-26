package dvoraka.avservice.common.amqp

import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.data.AvMessage
import org.springframework.amqp.core.Message
import org.springframework.amqp.support.converter.MessageConversionException
import spock.lang.Specification

/**
 * AV message converter test.
 */
class AvMessageConverterSpec extends Specification {

    AvMessageConverter converter


    def setup() {
        converter = new AvMessageConverter()
    }

    def "conversion to and from Message"() {
        given:
            AvMessage avMessage = Utils.genInfectedMessage()

        when:
            Message message = converter.toMessage(avMessage, null)
        and:
            AvMessage convertedBackAvMessage = (AvMessage) converter.fromMessage(message)

        then:
            avMessage == convertedBackAvMessage
    }

    def "conversion to from null"() {
        when:
            converter.toMessage(null, null)

        then:
            thrown(MessageConversionException)
    }

    def "conversion from null"() {
        when:
            converter.fromMessage(null)

        then:
            thrown(NullPointerException)
    }
}
