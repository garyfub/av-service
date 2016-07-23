package dvoraka.avservice.rest;

import dvoraka.avservice.MessageProcessor;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.MessageStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Implementation for a local calling.
 */
public class DirectRestStrategy implements RestStrategy {

    @Autowired
    private MessageProcessor restMessageProcessor;

    private static final Logger log = LogManager.getLogger(DirectRestStrategy.class.getName());

    private ExecutorService executorService;

    private CacheManager cacheManager;
    private Cache<String, AvMessage> messageCache;
    private boolean cacheUpdating;


    public DirectRestStrategy() {
        executorService = Executors.newSingleThreadExecutor();
        initializeCache();
    }

    private void initializeCache() {
        final long expirationTime = 10_000;
        CacheConfiguration<String, AvMessage> configuration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(String.class, AvMessage.class)
                .withExpiry(Expirations.timeToLiveExpiration(
                        new Duration(expirationTime, TimeUnit.MILLISECONDS)))
                .build();
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("restCache", configuration)
                .build(true);

        messageCache = cacheManager.getCache("restCache", String.class, AvMessage.class);
    }

    private void updateCache() {
        while (cacheUpdating) {
            if (restMessageProcessor.hasProcessedMessage()) {
                AvMessage message = restMessageProcessor.getProcessedMessage();
                messageCache.put(message.getCorrelationId(), message);
                log.debug("Saving message: " + message.getId());
            } else {
                final long sleepTime = 200;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.warn("Sleeping interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public MessageStatus messageStatus(String id) {
        return restMessageProcessor.messageStatus(id);
    }

    @Override
    public MessageStatus messageStatus(String id, String serviceId) {
        return messageStatus(id);
    }

    @Override
    public String messageServiceId(String id) {
        return null;
    }

    @Override
    public void messageCheck(AvMessage message) {
        restMessageProcessor.sendMessage(message);
    }

    @Override
    public AvMessage getResponse(String id) {
        return messageCache.get(id);
    }

    @PostConstruct
    @Override
    public void start() {
        log.debug("Starting cache updating...");
        cacheUpdating = true;
        executorService.execute(this::updateCache);
    }

    @Override
    @PreDestroy
    public void stop() {
        cacheUpdating = false;
        restMessageProcessor.stop();
        executorService.shutdown();
        cacheManager.close();
    }

    public void setRestMessageProcessor(MessageProcessor restMessageProcessor) {
        this.restMessageProcessor = restMessageProcessor;
    }
}
