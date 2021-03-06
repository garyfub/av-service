package dvoraka.avservice.core.configuration;

import dvoraka.avservice.avprogram.service.AvService;
import dvoraka.avservice.common.Utils;
import dvoraka.avservice.common.data.AvMessage;
import dvoraka.avservice.common.data.MessageType;
import dvoraka.avservice.core.AvCheckMessageProcessor;
import dvoraka.avservice.core.CompositeMessageProcessor;
import dvoraka.avservice.core.FileMessageProcessor;
import dvoraka.avservice.core.InputConditions;
import dvoraka.avservice.core.MessageProcessor;
import dvoraka.avservice.core.ProcessorConfiguration;
import dvoraka.avservice.db.service.MessageInfoService;
import dvoraka.avservice.storage.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.function.Predicate;

/**
 * Core storage configuration for import.
 */
@Configuration
@Profile("storage")
public class StorageCoreConfig {

    @Value("${avservice.cpuCores:2}")
    private Integer cpuCores;
    @Value("${avservice.serviceId:default1}")
    private String serviceId;


    @Bean
    @Profile("!replication")
    public MessageProcessor fileMessageProcessor(FileService fileService) {
        return new FileMessageProcessor(fileService);
    }

    @Bean
    public MessageProcessor checkMessageProcessor(
            AvService avService,
            MessageInfoService messageInfoService,
            Predicate<AvMessage> avCheckInputFilter
    ) {
        MessageProcessor messageProcessor = new AvCheckMessageProcessor(
                cpuCores,
                serviceId,
                avService,
                messageInfoService
        );
        messageProcessor.setInputFilter(avCheckInputFilter);

        return messageProcessor;
    }

    @Bean
    public MessageProcessor messageProcessor(
            MessageProcessor checkMessageProcessor,
            MessageProcessor fileMessageProcessor
    ) {
        List<InputConditions> checkConditions =
                new InputConditions.Builder()
                        .originalType(MessageType.FILE_CHECK)
                        .originalType(MessageType.FILE_SAVE)
                        .originalType(MessageType.FILE_UPDATE)
                        .build().toList();

        List<InputConditions> fileSaveUpdateConditions =
                new InputConditions.Builder()
                        .originalType(MessageType.FILE_SAVE)
                        .originalType(MessageType.FILE_UPDATE)
                        .condition((orig, last) -> Utils.OK_VIRUS_INFO.equals(last.getVirusInfo()))
                        .build().toList();

        List<InputConditions> fileLoadDeleteConditions =
                new InputConditions.Builder()
                        .originalType(MessageType.FILE_LOAD)
                        .originalType(MessageType.FILE_DELETE)
                        .build().toList();

        ProcessorConfiguration checkConfig = new ProcessorConfiguration(
                checkMessageProcessor,
                checkConditions,
                true
        );
        ProcessorConfiguration fileSaveUpdateConfig = new ProcessorConfiguration(
                fileMessageProcessor,
                fileSaveUpdateConditions,
                true
        );
        ProcessorConfiguration fileLoadDeleteConfig = new ProcessorConfiguration(
                fileMessageProcessor,
                fileLoadDeleteConditions,
                true
        );

        CompositeMessageProcessor processor = new CompositeMessageProcessor();
        processor.addProcessor(checkConfig);
        processor.addProcessor(fileSaveUpdateConfig);
        processor.addProcessor(fileLoadDeleteConfig);

        return processor;
    }
}
