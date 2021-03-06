package dvoraka.avservice.client.kafka;

import dvoraka.avservice.client.NetworkComponent;
import dvoraka.avservice.common.AvMessageListener;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.InfoSource;
import dvoraka.avservice.common.helper.AvMessageHelper;
import dvoraka.avservice.db.service.MessageInfoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;

/**
 * Kafka network component adapter.
 */
@Component
public class KafkaAdapter implements NetworkComponent, AvMessageHelper {

    private final String topic;
    private final String serviceId;
    private final KafkaTemplate<String, AvMessage> kafkaTemplate;
    private final MessageInfoService messageInfoService;

//    private static final Logger log = LogManager.getLogger(KafkaAdapter.class);

    private final List<AvMessageListener> listeners;


    @Autowired
    public KafkaAdapter(
            String topic,
            String serviceId,
            KafkaTemplate<String, AvMessage> kafkaTemplate,
            MessageInfoService messageInfoService
    ) {
        this.topic = requireNonNull(topic);
        this.serviceId = requireNonNull(serviceId);
        this.kafkaTemplate = requireNonNull(kafkaTemplate);
        this.messageInfoService = requireNonNull(messageInfoService);

        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void sendAvMessage(AvMessage message) {
        kafkaTemplate.send(topic, message);
        messageInfoService.save(message, InfoSource.KAFKA_ADAPTER_OUT, serviceId);
    }

    @Override
    public void onMessage(ConsumerRecord<String, AvMessage> record) {
        requireNonNull(record, "Record must not be null!");
        AvMessage avMessage = record.value();

        messageInfoService.save(avMessage, InfoSource.KAFKA_ADAPTER_IN, serviceId);

        notifyListeners(listeners, avMessage);
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void addAvMessageListener(AvMessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeAvMessageListener(AvMessageListener listener) {
        listeners.remove(listener);
    }
}
