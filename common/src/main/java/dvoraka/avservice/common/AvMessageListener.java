package dvoraka.avservice.common;

import dvoraka.avservice.common.data.AvMessage;

/**
 * AV message listener.
 */
@FunctionalInterface
public interface AvMessageListener {

    void onAVMessage(AvMessage message);
}
