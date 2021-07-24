package org.gethydra.redux.backend.download;

import java.util.LinkedList;
import java.util.logging.Logger;

public class DownloadManager
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private final LinkedList<Download> queue;

    public DownloadManager()
    {
        this.queue = new LinkedList<>();
    }

    public void queueDownload(Download download)
    {
        queue.add(download);
        log.warning("Queued download: " + download.getURL());
    }

    public Download getNext()
    {
        return queue.remove();
    }

    public boolean hasNext()
    {
        return !queue.isEmpty();
    }

    public long getTotalBytes()
    {
        long val = 0L;
        for (Download dl : queue)
            val += dl.getSize();
        return val;
    }
}
