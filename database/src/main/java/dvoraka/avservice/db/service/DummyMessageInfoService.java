package dvoraka.avservice.db.service;

import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.AvMessageSource;
import dvoraka.avservice.db.model.MessageInfo;

/**
 * Dummy service implementation.
 */
public class DummyMessageInfoService implements MessageInfoService {

    @Override
    public void save(AvMessage message, AvMessageSource source) {
    }

    @Override
    public MessageInfo getMessageInfo(String uuid) {
        return null;
    }
}