package dvoraka.avservice.rest.service

import dvoraka.avservice.MessageProcessor
import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.data.AvMessage
import dvoraka.avservice.common.data.DefaultAvMessage
import dvoraka.avservice.common.data.MessageStatus
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

/**
 * Local REST strategy spec.
 */
class LocalRestServiceSpec extends Specification {

    @Subject
    LocalRestService strategy

    MessageProcessor processor
    MessageProcessor compositeProcessor
    MessageProcessor fileProcessor
    String testId = 'TEST-ID'

    PollingConditions conditions


    def setup() {
        processor = Mock()
        compositeProcessor = Mock()
        fileProcessor = Mock()
        strategy = new LocalRestService(processor, fileProcessor, compositeProcessor)
        conditions = new PollingConditions(timeout: 2)
    }

    def cleanup() {
        strategy.stop()
    }

    def "unknown message status"() {
        setup:
            processor.messageStatus(_) >> MessageStatus.UNKNOWN
            strategy.start()

        expect:
            strategy.messageStatus('NEWID') == MessageStatus.UNKNOWN
            strategy.messageStatus('NEWID', 'SERVICEID') == MessageStatus.UNKNOWN
    }

    def "message check"() {
        setup:
            strategy.start()

        when:
            strategy.checkMessage(Utils.genMessage())

        then:
            1 * processor.sendMessage(_)
    }

    def "get message service ID returns null"() {
        when:
            strategy.start()

        then:
            strategy.messageServiceId('TEST') == null
    }

    def "get null response"() {
        setup:
            strategy.start()

        expect:
            strategy.getResponse('NEWID') == null
    }

    def "get real response"() {
        given:
            AvMessage request = new DefaultAvMessage.Builder(testId).build()
            AvMessage response = request.createResponse(Utils.OK_VIRUS_INFO)

        when:
            strategy.start()
            strategy.onAvMessage(response)

        then:
            conditions.eventually {
                strategy.getResponse(testId) == response
            }
    }
}
