package dvoraka.avservice.user.service

import spock.lang.Specification
import spock.lang.Subject

/**
 * LDAP user service spec.
 */
class LdapUserServiceSpec extends Specification {

    @Subject
    LdapUserService service


    def setup() {
        service = new LdapUserService()
    }

    def "use service"() {
        when:
            service.loadUserByUsername('some username')

        then:
            notThrown(Exception)
    }
}
