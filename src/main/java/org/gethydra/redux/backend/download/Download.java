package org.gethydra.redux.backend.download;

import org.gethydra.redux.Util;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.logging.Logger;

public class Download
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    public final String url;
    public final String dstFile;
    public final String sha1;
    public long size;
    public final boolean ignoreHash;

    public Download(String url, String dstFile, String sha1)
    {
        this(url, dstFile, sha1, false);
    }

    public Download(String url, String dstFile, String sha1, boolean ignoreHash)
    {
        this.url = url;
        this.dstFile = dstFile;
        this.sha1 = sha1;
        this.ignoreHash = ignoreHash;

        try
        {
            HttpsURLConnection httpConnection = (HttpsURLConnection) (new URL(this.url).openConnection());
            httpConnection.setReadTimeout(10000);
            size = httpConnection.getContentLength();
            httpConnection.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void execute(DownloadTracker tracker)
    {
        try
        {
            File outputFile = new File(dstFile);
            if (!ignoreHash)
            {
                if (outputFile.exists())
                {
                    String sha1disk = Util.calcSHA1(outputFile);
                    if (sha1disk.equalsIgnoreCase(sha1))
                    {
                        log.severe(String.format("Aborting download for '%s': file already exists", url));
                        return;
                    }
                }
            }

            HttpsURLConnection httpConnection = (HttpsURLConnection) (new URL(url).openConnection());

            httpConnection.setReadTimeout(10000);

            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            BufferedInputStream in = new java.io.BufferedInputStream(httpConnection.getInputStream());
            FileOutputStream fos = new java.io.FileOutputStream(outputFile);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int x = 0;
            while ((x = in.read(data, 0, 1024)) >= 0)
            {
                bout.write(data, 0, x);
                tracker.incrementDownloadedBytes(x);
                tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_TRACKER_UPDATE);
            }
            bout.close();
            in.close();

            if (!ignoreHash)
            {
                // recalculate hash to make sure the downloaded file is correct
                String sha1disk = Util.calcSHA1(outputFile);
                if (!sha1disk.equalsIgnoreCase(sha1))
                {
                    log.severe(String.format("Download failed for '%s': hash doesn't match", url));
                    if (!outputFile.delete())
                        outputFile.deleteOnExit();
                    tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_FAILED);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_FAILED);
        }
    }

    public String getURL()
    {
        return url;
    }

    public long getSize()
    {
        return size;
    }
}
