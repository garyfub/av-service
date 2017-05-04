package dvoraka.avservice.configuration;

import dvoraka.avservice.avprogram.configuration.AvProgramConfig;
import dvoraka.avservice.db.configuration.DatabaseConfig;
import dvoraka.avservice.storage.configuration.StorageConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Core module main configuration.
 */
@Configuration
@PropertySource("classpath:avservice.properties")
@EnableMBeanExport
@Profile("core")
@Import({
        CoreCheckConfig.class,
        CoreStorageConfig.class,
        CoreStorageReplicationConfig.class,

        AvProgramConfig.class,
        DatabaseConfig.class,
        StorageConfig.class
})
public class CoreConfig {

//    /**
//     * Special MBeanExporter bean for integration tests.
//     *
//     * @return MBeanExporter with a different registration strategy
//     */
//    @Bean
//    //TODO: temporary solution for runners with integration testing
////    @Profile("itest")
//    public MBeanExporter mBeanExporter() {
//        MBeanExporter exporter = new MBeanExporter();
//        exporter.setRegistrationPolicy(RegistrationPolicy.REPLACE_EXISTING);
//
//        return exporter;
//    }
}
