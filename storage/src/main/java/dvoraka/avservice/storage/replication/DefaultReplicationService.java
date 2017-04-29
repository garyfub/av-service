package dvoraka.avservice.storage.replication;

import dvoraka.avservice.client.service.ReplicationServiceClient;
import dvoraka.avservice.client.service.response.ReplicationMessageList;
import dvoraka.avservice.client.service.response.ReplicationResponseClient;
import dvoraka.avservice.common.data.Command;
import dvoraka.avservice.common.data.FileMessage;
import dvoraka.avservice.common.data.MessageRouting;
import dvoraka.avservice.common.data.ReplicationMessage;
import dvoraka.avservice.common.data.ReplicationStatus;
import dvoraka.avservice.common.replication.ReplicationHelper;
import dvoraka.avservice.storage.ExistingFileException;
import dvoraka.avservice.storage.FileNotFoundException;
import dvoraka.avservice.storage.FileServiceException;
import dvoraka.avservice.storage.service.FileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Default replication service implementation.
 */
@Service
public class DefaultReplicationService implements ReplicationService, ReplicationHelper {

    private final FileService fileService;
    private final ReplicationServiceClient serviceClient;
    private final ReplicationResponseClient responseClient;
    private final RemoteLock remoteLock;
    private final String nodeId;

    private static final Logger log = LogManager.getLogger(DefaultReplicationService.class);

    private static final int MAX_RESPONSE_TIME = 1_000; // one second
    private static final int DISCOVER_DELAY = 20_000;
    private static final int TERM_TIME = 10;
    private static final int REPLICATION_COUNT = 3;

    //    private BlockingQueue<ReplicationMessage> commands;

    private Set<String> neighbours;
    private int replicationCount;
    private ScheduledExecutorService executorService;


    @Autowired
    public DefaultReplicationService(
            FileService fileService,
            ReplicationServiceClient replicationServiceClient,
            ReplicationResponseClient replicationResponseClient,
            RemoteLock remoteLock,
            String nodeId
    ) {
        this.fileService = requireNonNull(fileService);
        this.serviceClient = requireNonNull(replicationServiceClient);
        this.responseClient = requireNonNull(replicationResponseClient);
        this.remoteLock = requireNonNull(remoteLock);
        this.nodeId = requireNonNull(nodeId);

//        final int size = 10;
//        commands = new ArrayBlockingQueue<>(size);

        neighbours = new CopyOnWriteArraySet<>();
        //TODO
        replicationCount = REPLICATION_COUNT;
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void start() {
        log.info("Starting service...");
        responseClient.addNoResponseMessageListener(this);
        executorService.schedule(remoteLock::start, 0L, TimeUnit.MILLISECONDS);
        executorService.scheduleWithFixedDelay(
                this::discoverNeighbours, 0L, DISCOVER_DELAY, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping service...");
        responseClient.removeNoResponseMessageListener(this);
        remoteLock.stop();
        shutdownAndAwaitTermination(executorService, TERM_TIME, log);
    }

    private void discoverNeighbours() {
        log.debug("Discovering neighbours...");

        ReplicationMessage message = createDiscoverRequest(nodeId);
        serviceClient.sendMessage(message);

        Optional<ReplicationMessageList> responses = responseClient
                .getResponseWait(message.getId(), MAX_RESPONSE_TIME);

        if (!responses.isPresent()) {
            log.debug("Discovered: 0");

            return;
        }

        ReplicationMessageList messages = responses.orElseGet(ReplicationMessageList::new);
        Set<String> newNeighbours = messages.stream()
                .filter(msg -> msg.getReplicationStatus() == ReplicationStatus.READY)
                .map(ReplicationMessage::getFromId)
                .collect(Collectors.toSet());

        neighbours.retainAll(newNeighbours);
        log.debug("Discovered: " + newNeighbours.size());
    }

    public int neighbourCount() {
        return neighbours.size();
    }

    @Override
    public void saveFile(FileMessage message) throws FileServiceException {
        log.debug("Save: " + message);

        if (exists(message)) {
            throw new ExistingFileException();
        }

        try {
            if (remoteLock.lockForFile(
                    message.getFilename(),
                    message.getOwner(),
                    neighbourCount())) {

                if (!exists(message)) {
                    fileService.saveFile(message);
                    sendSaveMessage(message);
                    //TODO: wait for response
                } else {
                    throw new ExistingFileException();
                }
            } else {
                log.warn("Save lock problem for: {}", message);
            }
        } catch (InterruptedException e) {
            log.warn("Locking interrupted!", e);
            Thread.currentThread().interrupt();
        } finally {
            remoteLock.unlockForFile(message.getFilename(), message.getOwner(), 0);
        }
    }

    private void sendSaveMessage(FileMessage message) {
        neighbours.stream()
                .map(id -> createSaveMessage(message, nodeId, id))
                .limit(getReplicationCount() - 1L)
                .forEach(serviceClient::sendMessage);
    }

    @Override
    public FileMessage loadFile(FileMessage message) throws FileServiceException {
        log.debug("Load: " + message);

        if (fileService.exists(message.getFilename(), message.getOwner())) {
            return fileService.loadFile(message);
        }

        if (exists(message)) {
            serviceClient.sendMessage(createLoadMessage(message, "neighbour"));

            Optional<ReplicationMessageList> replicationMessages = responseClient
                    .getResponseWait(message.getId(), MAX_RESPONSE_TIME);
            ReplicationMessageList messages = replicationMessages
                    .orElseGet(ReplicationMessageList::new);

            return messages.stream()
                    .findFirst()
                    .orElseThrow(FileNotFoundException::new);
        }

        throw new FileNotFoundException();
    }

    @Override
    public void updateFile(FileMessage message) throws FileServiceException {
        log.debug("Update: " + message);

        if (!exists(message)) {
            throw new FileNotFoundException();
        }

        if (fileService.exists(message)) {
            fileService.updateFile(message);
        }

        serviceClient.sendMessage(createUpdateMessage(message, "neighbour"));

//        ReplicationMessageList replicationMessages = responseClient.getResponseWait(
//                message.getId(), MAX_RESPONSE_TIME);
//        if (replicationMessages != null) {
//
//        }
    }

    @Override
    public void deleteFile(FileMessage message) throws FileServiceException {
        log.debug("Delete: " + message);

        if (!exists(message)) {
            return;
        }

        if (lockFile(message) && exists(message)) {
            fileService.deleteFile(message);
            sendDeleteMessage(message);
            //TODO: wait for response
        } else {
            log.warn("Delete problem for: {}", message);
        }

        remoteLock.unlockForFile(message.getFilename(), message.getOwner(), 0);
    }

    private void sendDeleteMessage(FileMessage message) {
        serviceClient.sendMessage(createDeleteBroadcast(message, nodeId));
    }

    private boolean lockFile(FileMessage message) {
        try {
            return remoteLock.lockForFile(
                    message.getFilename(), message.getOwner(), neighbourCount());
        } catch (InterruptedException e) {
            log.warn("Locking interrupted!", e);
            remoteLock.unlockForFile(message.getFilename(), message.getOwner(), 0);
            Thread.currentThread().interrupt();

            return false;
        }
    }

    @Override
    public boolean exists(String filename, String owner) {
        log.debug("Exists: {}, {}", filename, owner);

        if (fileService.exists(filename, owner)) {
            return true;
        }

        ReplicationMessage query = createExistsRequest(filename, owner, nodeId);
        serviceClient.sendMessage(query);

        Optional<ReplicationMessageList> response;
        response = responseClient.getResponseWait(query.getId(), MAX_RESPONSE_TIME);
        ReplicationMessageList messages = response.orElseGet(ReplicationMessageList::new);

        return messages.stream()
                .anyMatch(message -> message.getReplicationStatus() == ReplicationStatus.OK);
    }

    @Override
    public ReplicationStatus getStatus(FileMessage message) {
        log.debug("Status: " + message);

        serviceClient.sendMessage(createStatusRequest(message.getFilename(), message.getOwner()));

        ReplicationMessageList responses = responseClient.getResponse(message.getId());
        if (responses.stream()
                .filter(msg -> msg.getReplicationStatus().equals(ReplicationStatus.OK))
                .count() >= getReplicationCount()) {

            return ReplicationStatus.OK;
        }

        return ReplicationStatus.FAILED;
    }

    public int getReplicationCount() {
        return replicationCount;
    }

    @Override
    public void onMessage(ReplicationMessage message) {
        // broadcast and unicast messages from the replication network
        log.debug("On message ({}): {}", nodeId, message);

        // handle discover
        if (message.getRouting() == MessageRouting.BROADCAST
                && message.getCommand() == Command.DISCOVER) {
            serviceClient.sendMessage(createDiscoverReply(message, nodeId));
        }

        // handle exists
        if (message.getRouting() == MessageRouting.BROADCAST
                && message.getCommand() == Command.EXISTS
                && fileService.exists(message)) {
            serviceClient.sendMessage(createExistsReply(message, nodeId));
        }

        // handle save
        if (message.getRouting() == MessageRouting.UNICAST
                && message.getCommand() == Command.SAVE) {
            try {
                fileService.saveFile(message);
                serviceClient.sendMessage(createSaveSuccess(message, nodeId));
            } catch (FileServiceException e) {
                log.warn("Saving failed ({}): ", nodeId, e);
                serviceClient.sendMessage(createSaveFailed(message, nodeId));
            }
        }
    }
}
