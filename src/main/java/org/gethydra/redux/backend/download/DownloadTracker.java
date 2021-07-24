package org.gethydra.redux.backend.download;

import java.util.logging.Logger;

public class DownloadTracker
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private long totalBytes;
    private long downloadedBytes;
    private int exitCode;
    private EventHandler eventHandler;

    public long getTotalBytes()
    {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes)
    {
        log.warning("Set total bytes: " + totalBytes);
        this.totalBytes = totalBytes;
    }

    public long getDownloadedBytes()
    {
        return downloadedBytes;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    public void setExitCode(int exitCode)
    {
        this.exitCode = exitCode;
    }

    public void incrementDownloadedBytes(long downloadedBytes)
    {
        this.downloadedBytes += downloadedBytes;
    }

    public EventHandler getEventHandler()
    {
        return eventHandler;
    }

    public void setStatusHandler(EventHandler pipe)
    {
        eventHandler = pipe;
    }
}
