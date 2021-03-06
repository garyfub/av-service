package dvoraka.avservice.server;

import dvoraka.avservice.client.NetworkComponent;
import dvoraka.avservice.common.AvMessageListener;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.InfoSource;
import dvoraka.avservice.core.MessageProcessor;
import dvoraka.avservice.db.service.MessageInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;

/**
 * Server implementation
 */
@Service
public class BasicAvServer implements AvServer {

    private final NetworkComponent networkComponent;
    private final MessageProcessor messageProcessor;
    private final MessageInfoService messageInfoService;

    private static final Logger log = LogManager.getLogger(BasicAvServer.class);

    public static final InfoSource MESSAGE_SOURCE = InfoSource.SERVER;

    private final String serviceId;
    private final ProcessedAvMessageListener processedAvMessageListener;

    private volatile boolean started;
    private volatile boolean stopped = true;
    private volatile boolean running;


    @Autowired
    public BasicAvServer(
            String serviceId,
            NetworkComponent networkComponent,
            MessageProcessor messageProcessor,
            MessageInfoService messageInfoService
    ) {
        this.serviceId = requireNonNull(serviceId);
        this.networkComponent = requireNonNull(networkComponent);
        this.messageProcessor = requireNonNull(messageProcessor);
        this.messageInfoService = requireNonNull(messageInfoService);

        processedAvMessageListener = new ProcessedAvMessageListener();
    }

    @Override
    public void start() {
        log.info("Server started.");
        setStopped(false);
        setStarted(true);

        networkComponent.addAvMessageListener(this);
        messageProcessor.addProcessedAVMessageListener(processedAvMessageListener);

        setRunning(true);
        log.info("Server is running.");
    }

    @Override
    public void stop() {
        log.info("Server stopped.");
        setStopped(true);

        networkComponent.removeAvMessageListener(this);
        messageProcessor.removeProcessedAVMessageListener(processedAvMessageListener);

        setRunning(false);
        log.info("Server has stopped");
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void setRunning(boolean value) {
        this.running = value;
    }

    @Override
    public void onAvMessage(AvMessage message) {
        messageInfoService.save(message, MESSAGE_SOURCE, serviceId);
        messageProcessor.sendMessage(message);
    }

    private void setStarted(boolean started) {
        this.started = started;
    }

    private void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    @Override
    public void sendAvMessage(AvMessage message) {
        networkComponent.sendAvMessage(message);
    }

    class ProcessedAvMessageListener implements AvMessageListener {
        @Override
        public void onAvMessage(AvMessage message) {
            sendAvMessage(message);
        }
    }
}
