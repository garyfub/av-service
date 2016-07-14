package dvoraka.avservice.server;

/**
 * Abstract AV server.
 */
@Deprecated
public abstract class AbstractAvServer implements AvServer {

    private boolean started;
    private boolean stopped;
    private boolean running;

    @Override
    public abstract void start();

    public void setStarted() {
        started = true;
    }

    @Override
    public abstract void stop();

    public void setStopped() {
        stopped = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public abstract void restart();

    @Override
    public boolean isRunning() {
        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }
}