package dvoraka.avservice.client.service.response;

import dvoraka.avservice.common.ReplicationMessageListener;
import dvoraka.avservice.common.service.ServiceStatus;

import java.util.Optional;

/**
 * Client interface for getting a replication response.
 */
public interface ReplicationResponseClient extends ServiceStatus {

    /**
     * Returns a replication response message.
     *
     * @param id the request message ID
     * @return the response message or null if message is not available
     */
    ReplicationMessageList getResponse(String id);

    Optional<ReplicationMessageList> getResponseWait(String id, long maxWaitTime);

    Optional<ReplicationMessageList> getResponseWait(String id, long minWaitTime, long maxWaitTime);

    Optional<ReplicationMessageList> getResponseWaitSize(String id, long maxWaitTime, int size);

    void addNoResponseMessageListener(ReplicationMessageListener listener);

    void removeNoResponseMessageListener(ReplicationMessageListener listener);
}
