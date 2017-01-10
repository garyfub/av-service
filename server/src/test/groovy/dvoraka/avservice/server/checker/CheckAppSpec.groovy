package dvoraka.avservice.server.checker

import spock.lang.Specification
import spock.lang.Subject

/**
 * Check app spec.
 */
class CheckAppSpec extends Specification {

    @Subject
    CheckApp checkApp

    Checker checker


    def setup() {
        checker = Mock()
        checkApp = new CheckApp(checker)
    }

    def "start"() {
        when:
            checkApp.start()

        then:
            1 * checker.check()
    }
}
