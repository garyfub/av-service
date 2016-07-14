package dvoraka.avservice.service

import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.data.AVMessageType
import dvoraka.avservice.common.data.AvMessage
import dvoraka.avservice.common.data.MessageStatus
import dvoraka.avservice.configuration.ServiceConfig
import dvoraka.avservice.rest.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * REST testing.
 */
@ContextConfiguration(classes = [ServiceConfig.class])
class RestServiceISpec extends Specification {

    @Autowired
    RestClient client;


    def "get testing message"() {
        setup:
        AvMessage message = client.getMessage("/gen-msg")

        expect:
        message != null
        message.getServiceId().equals("testing-service")
    }

    def "send normal message"() {
        setup:
        AvMessage message = Utils.genNormalMessage()

        expect:
        client.postMessage(message, "/msg-check")
    }

    def "check normal message"() {
        setup:
        AvMessage message = Utils.genNormalMessage()
        String id = message.getId()

        client.postMessage(message, "/msg-check")
        sleep(2000)

        MessageStatus status = client.getMessageStatus("/msg-status/" + id)
        AvMessage response = client.getMessage("/get-response/" + id)

        expect:
        status == MessageStatus.PROCESSED
        response.type == AVMessageType.RESPONSE
        response.getVirusInfo().equals("")
    }

    def "send infected message"() {
        setup:
        AvMessage message = Utils.genInfectedMessage()

        expect:
        client.postMessage(message, "/msg-check")
    }

    def "check infected message"() {
        setup:
        AvMessage message = Utils.genInfectedMessage()
        String id = message.getId()

        client.postMessage(message, "/msg-check")
        sleep(2000)

        MessageStatus status = client.getMessageStatus("/msg-status/" + id)
        AvMessage response = client.getMessage("/get-response/" + id)

        expect:
        status == MessageStatus.PROCESSED
        response.type == AVMessageType.RESPONSE
        !response.getVirusInfo().equals("")
    }
}
