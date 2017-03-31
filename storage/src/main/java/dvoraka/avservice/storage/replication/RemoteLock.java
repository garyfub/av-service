package dvoraka.avservice.storage.replication;

/**
 * Remote lock interface.
 */
public interface RemoteLock {

    void start();

    void stop();

    boolean lockForFile(String filename, String owner) throws InterruptedException;

    boolean unlockForFile(String filename, String owner);
}
