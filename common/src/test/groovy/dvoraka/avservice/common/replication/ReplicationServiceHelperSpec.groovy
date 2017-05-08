package dvoraka.avservice.common.replication

import dvoraka.avservice.common.data.Command
import dvoraka.avservice.common.data.MessageRouting
import dvoraka.avservice.common.data.MessageType
import dvoraka.avservice.common.data.ReplicationMessage
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/**
 * ReplicationServiceHelperSpec.
 */
class ReplicationServiceHelperSpec extends Specification {

    @Subject
    ReplicationServiceHelper helper

    ReplicationMessage result

    @Shared
    String testFilename = 'testFilename'
    @Shared
    String testOwner = 'testOwner'
    @Shared
    String testId = 'testID'
    @Shared
    String otherId = 'otherID'
    @Shared
    long testSequence = 999L


    def setup() {
        helper = Spy()
    }

    def "exists request"() {
        when:
            result = helper.createExistsRequest(testFilename, testOwner, testId)

        then:
            checkRequestBase(result)
            result.getCommand() == Command.EXISTS
    }

    def "status request"() {
        when:
            result = helper.createStatusRequest(testFilename, testOwner, testId)

        then:
            checkRequestBase(result)
            result.getCommand() == Command.STATUS
    }

    def "discover request"() {
        when:
            result = helper.createDiscoverRequest(testId)

        then:
            result.getCommand() == Command.DISCOVER

            result.getType() == MessageType.REPLICATION_SERVICE
            result.getRouting() == MessageRouting.BROADCAST

            result.getFromId() == testId
            result.getToId() == null
    }

    def "lock request"() {
        when:
            result = helper.createLockRequest(testFilename, testOwner, testId, testSequence)

        then:
            checkRequestBase(result)
            result.getCommand() == Command.LOCK
            result.getSequence() == testSequence
    }

    def "unlock request"() {
        when:
            result = helper.createUnlockRequest(testFilename, testOwner, testId, testSequence)

        then:
            checkRequestBase(result)
            result.getCommand() == Command.UNLOCK
            result.getSequence() == testSequence
    }

    def "sequence request"() {
        when:
            result = helper.createSequenceRequest(testId)

        then:
            result.getCommand() == Command.SEQUENCE

            result.getType() == MessageType.REPLICATION_SERVICE
            result.getRouting() == MessageRouting.BROADCAST

            result.getFromId() == testId
            result.getToId() == null
    }

    void checkRequestBase(ReplicationMessage message) {
        assert message.getType() == MessageType.REPLICATION_SERVICE
        assert message.getRouting() == MessageRouting.BROADCAST

        assert message.getFromId() == testId
        assert message.getToId() == null

        assert message.getFilename() == testFilename
        assert message.getOwner() == testOwner
    }
}
