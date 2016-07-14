package dvoraka.avservice.rest;

import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.AVMessageType;
import dvoraka.avservice.common.data.DefaultAvMessage;
import dvoraka.avservice.common.data.MessageStatus;
import dvoraka.avservice.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AV REST controller.
 */
@RestController
public class AvController {

    @Autowired
    private RestService restService;

    /**
     * Testing method.
     *
     * @param info the info String
     * @return the info
     */
    @RequestMapping(value = "/")
    public String info(@RequestParam(value = "info", defaultValue = "AV service") String info) {
        return info;
    }

    /**
     * Returns the message status.
     *
     * @param id the message ID
     * @return the status
     */
    @RequestMapping(value = "/msg-status/{id}", method = RequestMethod.GET)
    public MessageStatus messageStatus(@PathVariable String id) {
        return restService.messageStatus(id);
    }

    /**
     * Returns the message status.
     *
     * @param id        the message ID
     * @param serviceId the service ID
     * @return the status
     */
    @RequestMapping(value = "/msg-status/{id}/{serviceId}", method = RequestMethod.GET)
    public MessageStatus messageStatus(@PathVariable String id, @PathVariable String serviceId) {
        return restService.messageStatus(id, serviceId);
    }

    /**
     * Returns the service ID for the message ID.
     *
     * @param id the message ID
     * @return the service ID
     */
    @RequestMapping(value = "/msg-service-id/{id}", method = RequestMethod.GET)
    public String messageServiceId(@PathVariable String id) {
        return restService.messageServiceId(id);
    }

    @RequestMapping(value = "/msg-check", method = RequestMethod.POST)
    public ResponseEntity<String> messageCheck(@RequestBody DefaultAvMessage message) {

        restService.messageCheck(message);

        return new ResponseEntity<String>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/get-response/{id}", method = RequestMethod.GET)
    public AvMessage getResponse(@PathVariable String id) {
        return restService.getResponse(id);
    }

    /**
     * Generates a testing AV message.
     *
     * @return the generated message
     */
    @RequestMapping(value = "/gen-msg")
    public AvMessage generateMessage() {
        final int dataSize = 10;
        return new DefaultAvMessage.Builder(null)
                .serviceId("testing-service")
                .virusInfo("bad")
                .correlationId("corrId")
                .data(new byte[dataSize])
                .type(AVMessageType.RESPONSE)
                .build();
    }
}
