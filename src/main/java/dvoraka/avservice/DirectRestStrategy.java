package dvoraka.avservice;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation for a local calling.
 */
public class DirectRestStrategy implements RestStrategy {

    @Autowired
    private MessageProcessor messageProcessor;
}
