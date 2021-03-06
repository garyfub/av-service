package dvoraka.avservice.rest.service;

import dvoraka.avservice.client.service.AvServiceClient;
import dvoraka.avservice.client.service.FileServiceClient;
import dvoraka.avservice.client.service.response.ResponseProvider;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.MessageStatus;

/**
 * Main REST service.
 */
public interface RestService
        extends AvServiceClient, FileServiceClient, ResponseProvider<AvMessage> {

    /**
     * Returns a message status.
     *
     * @param id the message ID
     * @return the status
     */
    MessageStatus messageStatus(String id);

    /**
     * Returns a response message.
     *
     * @param id the request message ID
     * @return the response message or null if message is not available
     */
    AvMessage getResponse(String id);

    /**
     * Starts the service.
     */
    void start();

    /**
     * Stops the service.
     */
    void stop();
}
