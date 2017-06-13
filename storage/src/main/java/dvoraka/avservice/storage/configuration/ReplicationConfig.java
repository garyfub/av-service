package dvoraka.avservice.storage.configuration;

import dvoraka.avservice.client.service.ReplicationServiceClient;
import dvoraka.avservice.client.service.response.ReplicationResponseClient;
import dvoraka.avservice.common.service.BasicServiceManagement;
import dvoraka.avservice.storage.replication.DefaultRemoteLock;
import dvoraka.avservice.storage.replication.DefaultReplicationService;
import dvoraka.avservice.storage.replication.RemoteLock;
import dvoraka.avservice.storage.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Replication configuration for the import.
 */
@Configuration
@Profile("replication")
public class ReplicationConfig {

    @Value("${avservice.storage.replication.nodeId}")
    private String nodeId;


    @Bean
    public FileService replicationService(
            FileService fileService,
            ReplicationServiceClient replicationServiceClient,
            ReplicationResponseClient replicationResponseClient,
            RemoteLock remoteLock
    ) {
        return new DefaultReplicationService(
                fileService,
                replicationServiceClient,
                replicationResponseClient,
                remoteLock,
                nodeId
        );
    }

    @Bean
    public RemoteLock remoteLock(
            ReplicationServiceClient serviceClient,
            ReplicationResponseClient responseClient
    ) {
        return new DefaultRemoteLock(serviceClient, responseClient, nodeId);
    }

    @Bean
    public BasicServiceManagement replicationServiceApp() {
        return new BasicServiceManagement();
    }
}
