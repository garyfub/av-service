package dvoraka.avservice.server.amqp;

import dvoraka.avservice.common.AVMessageListener;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.AVMessageMapper;
import dvoraka.avservice.common.exception.MapperException;
import dvoraka.avservice.server.ServerComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * AMQP component.
 */
public class AmqpComponent implements ServerComponent {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger log = LogManager.getLogger(AmqpComponent.class.getName());

    private String responseExchange;
    private List<AVMessageListener> listeners = new ArrayList<>();


    public AmqpComponent(String responseExchange) {
        this.responseExchange = responseExchange;
    }

    @Override
    public void onMessage(Message message) {
        AvMessage avMessage = null;
        try {
            avMessage = AVMessageMapper.transform(message);
        } catch (MapperException e) {
            log.warn("Transformation error!", e);
        }

        for (AVMessageListener listener : listeners) {
            listener.onAVMessage(avMessage);
        }
    }

    @Override
    public void addAVMessageListener(AVMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeAVMessageListener(AVMessageListener listener) {
        listeners.remove(listener);
    }

    public int listenersCount() {
        return listeners.size();
    }

    @Override
    public void sendMessage(AvMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message may not be null!");
        }

        Message response = null;
        try {
            response = AVMessageMapper.transform(message);
            rabbitTemplate.send(responseExchange, "ROUTINGKEY", response);
        } catch (MapperException e) {
            log.warn("Message transformation problem!", e);
            // create error response
            AvMessage errorResponse = message.createErrorResponse(e.getMessage());
            try {
                response = AVMessageMapper.transform(errorResponse);
            } catch (MapperException e1) {
                log.error("Message send error!", e1);
            }
            rabbitTemplate.send(responseExchange, "ROUTINGKEY", response);
        }
    }
}