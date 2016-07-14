package dvoraka.avservice

import dvoraka.avservice.common.ReceivingType
import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.data.AvMessage
import dvoraka.avservice.common.data.DefaultAvMessage
import dvoraka.avservice.common.data.MessageStatus
import dvoraka.avservice.common.exception.ScanErrorException
import dvoraka.avservice.service.AvService
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * Default processor tests.
 */
class DefaultMessageProcessorSpec extends Specification {

    DefaultMessageProcessor processor
    PollingConditions conditions


    def setup() {
        processor = new DefaultMessageProcessor(2)
        conditions = new PollingConditions(timeout: 2)
    }

    def cleanup() {
        if (processor != null) {
            processor.cleanup()
        }
    }

    def "constructor (thread count)"() {
        setup:
        int threadCount = 5
        processor = new DefaultMessageProcessor(threadCount)

        expect:
        processor.getThreadCount() == threadCount
        processor.getQueueSize() == DefaultMessageProcessor.DEFAULT_QUEUE_SIZE
        processor.getServerReceivingType() ==
                DefaultMessageProcessor.DEFAULT_RECEIVING_TYPE
    }

    def "constructor (thread count, rec. type, queue size)"() {
        setup:
        int threadCount = 5
        ReceivingType receivingType = ReceivingType.LISTENER
        int queueSize = 10
        processor = new DefaultMessageProcessor(
                threadCount,
                receivingType,
                queueSize)

        expect:
        processor.getThreadCount() == threadCount
        processor.getServerReceivingType() == receivingType
        processor.getQueueSize() == queueSize
    }

    def "send normal message"() {
        setup:
        AvService service = Stub()
        service.scanStream(_) >> false

        setProcessorService(service)

        AvMessage message = Utils.genNormalMessage()
        processor.sendMessage(message)

        expect:
        conditions.eventually {
            processor.hasProcessedMessage()
            processor.getProcessedMessage().getCorrelationId().equals(message.getId())
        }
    }

    def "responding test with a listener"() {
        given:
        AvService service = Stub()
        service.scanStream(_) >> false

        AvMessage response = null
        ProcessedAVMessageListener messageListener = new ProcessedAVMessageListener() {
            @Override
            void onProcessedAVMessage(AvMessage message) {
                response = message
            }
        }

        processor = new DefaultMessageProcessor(2, ReceivingType.LISTENER, 10)
        setProcessorService(service)
        processor.addProcessedAVMessageListener(messageListener)

        AvMessage message = Utils.genNormalMessage()

        when:
        processor.sendMessage(message)

        then:
        conditions.eventually {
            response != null
            response.getCorrelationId() == message.getId()
        }
    }

    def "add and remove listeners"() {
        given:
        processor = new DefaultMessageProcessor(2, ReceivingType.LISTENER, 10)
        ProcessedAVMessageListener messageListener = Mock()

        when:
        processor.addProcessedAVMessageListener(messageListener)

        then:
        processor.observersCount() == 1

        when:
        processor.removeProcessedAVMessageListener(messageListener)

        then:
        processor.observersCount() == 0
    }

    def "add a listener for polling type"() {
        when:
        processor.addProcessedAVMessageListener(null)

        then:
        thrown(UnsupportedOperationException)
    }

    def "ask for a message with a listener"() {
        given:
        processor = new DefaultMessageProcessor(2, ReceivingType.LISTENER, 10)

        when:
        processor.hasProcessedMessage()

        then:
        thrown(UnsupportedOperationException)

        when:
        processor.getProcessedMessage()

        then:
        thrown(UnsupportedOperationException)
    }

    def "send message with a full queue"() {
        setup:
        AvService service = Stub()
        service.scanStream(_) >> false

        processor = new DefaultMessageProcessor(2, ReceivingType.POLLING, 1)
        setProcessorService(service)

        AvMessage message1 = Utils.genNormalMessage()
        processor.sendMessage(message1)
        AvMessage message2 = Utils.genNormalMessage()
        processor.sendMessage(message2)

        expect:
        conditions.eventually {
            processor.isProcessedQueueFull()
            processor.hasProcessedMessage()
            processor.getProcessedMessage()
        }

        and:
        conditions.eventually {
            processor.hasProcessedMessage()
            processor.getProcessedMessage()
        }
        conditions.eventually {
            !processor.isProcessedQueueFull()
        }
    }

    def "send message with a service error"() {
        given:
        AvService service = Stub()
        service.scanStream(_) >> {
            throw new ScanErrorException("Service is dead")
        }

        setProcessorService(service)
        AvMessage message = Utils.genNormalMessage()

        when:
        processor.sendMessage(message)

        then:
        notThrown(ScanErrorException)
    }

    def "processing message status"() {
        given:
        String testId = "testId"

        AvService service = Stub()
        service.scanStream(_) >> {
            sleep(1000)
            return false
        }

        setProcessorService(service)

        when:
        processor.sendMessage(new DefaultAvMessage.Builder(testId).build())

        then:
        processor.messageStatus(testId) == MessageStatus.PROCESSING
    }

    def "processed message status"() {
        given:
        String testId = "testId"

        AvService service = Stub()
        setProcessorService(service)

        when:
        processor.sendMessage(new DefaultAvMessage.Builder(testId).build())

        then:
        conditions.eventually {
            processor.messageStatus(testId) == MessageStatus.PROCESSED
        }
    }

    def "unknown message status"() {
        setup:
        String testId = "testId"

        AvService service = Stub()
        setProcessorService(service)

        expect:
        processor.messageStatus(testId) == MessageStatus.UNKNOWN
    }

    def "test message counters"() {
        given:
        AvService service = Stub()
        service.scanStream(_) >> false

        setProcessorService(service)

        when:
        AvMessage message = Utils.genNormalMessage()
        messageCount.times {
            processor.sendMessage(message)
        }

        then:
        conditions.eventually {
            processor.getReceivedMsgCount() == messageCount
            processor.getProcessedMsgCount() == messageCount
        }

        where:
        messageCount << [1, 3]
    }

    void setProcessorService(AvService service) {
        ReflectionTestUtils.setField(processor, null, service, AvService.class)
    }
}
