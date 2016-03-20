package dvoraka.avservice.data;

import dvoraka.avservice.exception.MapperException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * AVMessage mapper
 */
public class AVMessageMapper {

    public static AVMessage transform(Message msg) throws MapperException {
        MessageProperties msgProps = msg.getMessageProperties();

        return new DefaultAVMessage.Builder(msgProps.getMessageId())
                .data(msg.getBody())
//                .type(AVMessageType.valueOf(msgProps.getType()))
                .build();
    }

    public static Message transform(AVMessage msg) throws MapperException {
        MessageProperties props = new MessageProperties();
        props.setMessageId(msg.getId());
        props.setCorrelationId(msg.getCorrelationId().getBytes());
        props.setAppId("antivirus");
        props.setType(msg.getType().toString());

        props.setHeader("isClean", 0);

        return new Message(msg.getData(), props);
    }
}