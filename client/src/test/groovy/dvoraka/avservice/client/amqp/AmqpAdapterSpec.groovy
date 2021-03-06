package dvoraka.avservice.client.amqp

import dvoraka.avservice.common.AvMessageListener
import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.amqp.AvMessageMapper
import dvoraka.avservice.common.data.AvMessage
import dvoraka.avservice.db.repository.db.DbMessageInfoRepository
import dvoraka.avservice.db.service.DbMessageInfoService
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessageConversionException
import org.springframework.amqp.support.converter.MessageConverter
import spock.lang.Specification
import spock.lang.Subject

/**
 * AMQP component test.
 */
class AmqpAdapterSpec extends Specification {

    @Subject
    AmqpAdapter component

    RabbitTemplate rabbitTemplate
    AvMessageMapper messageMapper
    MessageConverter converter

    String testExchange = 'TEST-EXCHANGE'


    def setup() {
        DbMessageInfoRepository infoRepository = Mock()
        DbMessageInfoService infoService = new DbMessageInfoService(infoRepository)

        converter = Mock()
        converter.fromMessage(_) >> Mock(AvMessage)

        rabbitTemplate = Mock()
        rabbitTemplate.getMessageConverter() >> converter

        component = new AmqpAdapter(testExchange, "TEST1", rabbitTemplate, infoService)

        messageMapper = new AvMessageMapper()
    }

    def "on message"() {
        given:
            AvMessageListener listener = Mock()
            AvMessage message = Utils.genMessage()
            Message amqpMsg = messageMapper.transform(message)

            component.addAvMessageListener(listener)

        when:
            component.onMessage(amqpMsg)

        then:
            1 * listener.onAvMessage(_)
    }

    def "on message with conversion exception"() {
        given:
            AvMessageListener listener = Mock()
            AvMessage message = Utils.genMessage()
            Message amqpMsg = messageMapper.transform(message)
            amqpMsg.getMessageProperties().setType(null)

            component.addAvMessageListener(listener)

        when:
            component.onMessage(amqpMsg)

        then:
            converter.fromMessage(amqpMsg) >> {
                throw new MessageConversionException("TEST conversion")
            }
            0 * listener.onAvMessage(_)
    }

    def "on message with null"() {
        when:
            component.onMessage((Message) null)

        then:
            thrown(NullPointerException)
    }

    def "send null message"() {
        when:
            component.sendAvMessage(null)

        then:
            thrown(NullPointerException)
    }

    def "send normal message"() {
        given:
            AvMessage message = Utils.genMessage()

        when:
            component.sendAvMessage(message)

        then:
            1 * rabbitTemplate.convertAndSend(_, _, _)
    }

    def "send message with conversion error"() {
        given:
            AvMessage message = Utils.genMessage()

        when:
            component.sendAvMessage(message)

        then:
            1 * rabbitTemplate.convertAndSend(testExchange, _ as String, message) >> {
                throw new MessageConversionException("Conversion problem")
            }

            1 * rabbitTemplate.convertAndSend(testExchange, _ as String, _)
    }

    def "send message with AMQP exception"() {
        given:
            AvMessage message = Utils.genMessage()

            rabbitTemplate.convertAndSend(testExchange, _ as String, message) >> {
                throw new AmqpException("Problem!")
            }

        when:
            component.sendAvMessage(message)

        then:
            notThrown(Exception)
    }

    def "add listeners"() {
        when:
            component.addAvMessageListener(getAvMessageListener())
            component.addAvMessageListener(getAvMessageListener())

        then:
            component.listenersCount() == 2
    }

    def "remove listeners"() {
        given:
            AvMessageListener listener1 = getAvMessageListener()
            AvMessageListener listener2 = getAvMessageListener()

        when:
            component.addAvMessageListener(listener1)
            component.addAvMessageListener(listener2)

        then:
            component.listenersCount() == 2

        when:
            component.removeAvMessageListener(listener1)
            component.removeAvMessageListener(listener2)

        then:
            component.listenersCount() == 0
    }

    def "add listeners from diff threads"() {
        given:
            int observers = 50

            Runnable addListener = {
                component.addAvMessageListener(getAvMessageListener())
            }

            Thread[] threads = new Thread[observers]
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(addListener)
            }

        when:
            threads.each {
                it.start()
            }
            threads.each {
                it.join()
            }

        then:
            observers == component.listenersCount()
    }

    def "remove observers from different threads"() {
        given:
            int observers = 50

            AvMessageListener messageListener = getAvMessageListener()
            Runnable removeListener = {
                component.removeAvMessageListener(messageListener)
            }

            Thread[] threads = new Thread[observers]
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(removeListener)
            }

            observers.times {
                component.addAvMessageListener(messageListener)
            }

        when:
            threads.each {
                it.start()
            }
            threads.each {
                it.join()
            }

        then:
            component.listenersCount() == 0
    }

    def "run wrong onMessage"() {
        when:
            component.onMessage((javax.jms.Message) null)

        then:
            thrown(UnsupportedOperationException)
    }

    AvMessageListener getAvMessageListener() {
        return new AvMessageListener() {
            @Override
            void onAvMessage(AvMessage message) {
            }
        }
    }
}
