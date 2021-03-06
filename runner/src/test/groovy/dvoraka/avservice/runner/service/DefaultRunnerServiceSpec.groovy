package dvoraka.avservice.runner.service

import dvoraka.avservice.runner.DummyRunnerConfiguration
import dvoraka.avservice.runner.RunnerConfiguration
import dvoraka.avservice.runner.RunnerNotFoundException
import dvoraka.avservice.runner.RunningState
import spock.lang.Specification
import spock.lang.Subject

class DefaultRunnerServiceSpec extends Specification {

    @Subject
    DefaultRunnerService service

    RunnerConfiguration configuration


    def setup() {
        configuration = new DummyRunnerConfiguration()
        service = new DefaultRunnerService()
    }

    def cleanup() {
        service.stop()
    }

    def "create runner"() {
        when:
            service.createRunner(configuration)

        then:
            service.getRunnerState(configuration.getId()) == RunningState.UNKNOWN
    }

    def "list configurations"() {
        when:
            service.createRunner(configuration)

        then:
            service.listRunners().size() == 1
            service.listRunners().stream().findAny().orElse("") == configuration.getId()
    }

    def "create runner and start"() {
        when:
            service.createRunner(configuration)

        then:
            service.getRunnerState(configuration.getId()) == RunningState.UNKNOWN

        when:
            service.startRunner(configuration.getId())
            sleep(100)

        then:
            service.getRunnerState(configuration.getId()) == RunningState.RUNNING
    }

    def "create runner, start and stop"() {
        when:
            service.createRunner(configuration)

        then:
            service.getRunnerState(configuration.getId()) == RunningState.UNKNOWN

        when:
            service.startRunner(configuration.getId())
            sleep(100)

        then:
            service.getRunnerState(configuration.getId()) == RunningState.RUNNING

        when:
            service.stopRunner(configuration.getId())

        then:
            service.getRunnerState(configuration.getId()) == RunningState.STOPPED
    }

    def "start with unknown ID"() {
        when:
            service.startRunner("aaaaaa")

        then:
            thrown(RunnerNotFoundException)
    }

    def "stop with unknown ID"() {
        when:
            service.stopRunner("aaaaaa")

        then:
            thrown(RunnerNotFoundException)
    }

    def "state for unknown ID"() {
        when:
            service.stopRunner("aaaaaa")

        then:
            thrown(RunnerNotFoundException)
    }
}
