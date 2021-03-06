package dvoraka.avservice.common;

import dvoraka.avservice.common.data.replication.ReplicationMessage;

/**
 * Replication message listener.
 */
@FunctionalInterface
public interface ReplicationMessageListener {

    /**
     * Receives replication messages.
     *
     * @param message the message
     */
    void onMessage(ReplicationMessage message);
}
