package dvoraka.avservice.server.configuration.jms;

import dvoraka.avservice.client.ServerComponent;
import dvoraka.avservice.client.checker.CheckApp;
import dvoraka.avservice.client.checker.Checker;
import dvoraka.avservice.client.checker.PerformanceTester;
import dvoraka.avservice.client.checker.SimpleChecker;
import dvoraka.avservice.common.testing.PerformanceTestProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * JMS checker configuration for import.
 */
@Configuration
@Profile("jms-checker")
public class JmsCheckerConfig {

    @Value("${avservice.jms.checkDestination:check}")
    private String checkDestination;
    @Value("${avservice.jms.resultDestination:result}")
    private String resultDestination;

    @Value("${avservice.serviceId:default1}")
    private String serviceId;


    @Bean
    public Checker checker(ServerComponent serverComponent) {
        return new SimpleChecker(serverComponent);
    }

    @Bean
    public PerformanceTester defaultLoadTester(
            Checker checker,
            PerformanceTestProperties testProperties
    ) {
        return new PerformanceTester(checker, testProperties);
    }

    @Bean
    public CheckApp checkApp(Checker checker) {
        return new CheckApp(checker);
    }
}
