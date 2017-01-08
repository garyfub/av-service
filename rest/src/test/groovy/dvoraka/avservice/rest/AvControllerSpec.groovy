package dvoraka.avservice.rest

import com.fasterxml.jackson.databind.ObjectMapper
import dvoraka.avservice.common.Utils
import dvoraka.avservice.common.data.AvMessage
import dvoraka.avservice.common.data.MessageStatus
import dvoraka.avservice.rest.controller.AvController
import dvoraka.avservice.rest.service.RestService
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * AV controller spec.
 */
class AvControllerSpec extends Specification {

    MockMvc mockMvc
    RestService service


    def setup() {
        service = Stub()
        mockMvc = MockMvcBuilders.standaloneSetup(new AvController(service))
                .build()
    }

    def "test info"() {
        setup:
            ResultActions response = mockMvc.perform(
                    get("/"))

        expect:
            response
                    .andExpect(status().isOk())
                    .andExpect(content().string("AV service"))
    }

    def "test messageStatus(String)"() {
        setup:
            MessageStatus messageStatus = MessageStatus.PROCESSED
            String messageId = 'TID'
            service.messageStatus(messageId) >> messageStatus

            ResultActions response = mockMvc.perform(
                    get("/msg-status/${messageId}"))

            ObjectMapper mapper = new ObjectMapper()
            String expectedContent = mapper.writeValueAsString(messageStatus)

        expect:
            response
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedContent))
    }

    def "test messageStatus(String, String)"() {
        setup:
            MessageStatus messageStatus = MessageStatus.PROCESSING
            String messageId = 'TID'
            String serviceId = "SID"
            service.messageStatus(messageId, serviceId) >> messageStatus

            ResultActions response = mockMvc.perform(
                    get("/msg-status/${messageId}/${serviceId}"))

            ObjectMapper mapper = new ObjectMapper()
            String expectedContent = mapper.writeValueAsString(messageStatus)

        expect:
            response
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedContent))
    }

    def "test messageServiceId(String)"() {
        setup:
            String messageId = 'TID'
            String serviceId = "SID"
            service.messageServiceId(messageId) >> serviceId

            ResultActions response = mockMvc.perform(
                    get("/msg-service-id/${messageId}"))

        expect:
            response
                    .andExpect(status().isOk())
                    .andExpect(content().string(serviceId))
    }

    def "test messageCheck(AVMessage)"() {
        setup:
            AvMessage message = Utils.genNormalMessage()

            ObjectMapper mapper = new ObjectMapper()
            String content = mapper.writeValueAsString(message)

            ResultActions response = mockMvc.perform(
                    post("/msg-check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))

        expect:
            response
                    .andExpect(status().isAccepted())
    }

    def "test messageCheck(AVMessage without ID)"() {
        setup:
            AvMessage message = Utils.genNormalMessage()
            ReflectionTestUtils.setField(message, "id", null, null)

            ObjectMapper mapper = new ObjectMapper()
            String content = mapper.writeValueAsString(message)

            ResultActions response = mockMvc.perform(
                    post("/msg-check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))

        expect: "error response"
            response
                    .andExpect(status().is4xxClientError())
    }

    def "test getResponse(String)"() {
        setup:
            String messageId = "TID"
            AvMessage responseMsg = Utils.genNormalMessage()
            service.getResponse(messageId) >> responseMsg

            ResultActions response = mockMvc.perform(
                    get("/get-response/${messageId}"))

            ObjectMapper mapper = new ObjectMapper()
            String expectedContent = mapper.writeValueAsString(responseMsg)

        expect:
            response
                    .andExpect(status().isOk())
                    .andExpect(content().string(expectedContent))
    }

    def "test generateMessage"() {
        setup:
            ResultActions response = mockMvc.perform(
                    get("/gen-msg"))

        expect:
            response
                    .andExpect(status().isOk())

    }
}
