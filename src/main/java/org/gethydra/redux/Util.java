package org.gethydra.redux;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Util
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    public static File getLinuxHomeDirectory()
    {
        String linux_home = System.getenv("HOME");
        String linux_user = System.getenv("USER");
        if (linux_user == "root")
            linux_home = "/root";
        else
            linux_home = "/home/" + linux_user;
        return new File(linux_home);
    }

    public static void openNetpage(String url)
    {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String get(String url)
    {
        try
        {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            InputStream is = responseEntity.getContent();
            String contentEncoding = "UTF-8";
            return IOUtils.toString(is, contentEncoding);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static File getHydraDirectory()
    {
        switch (OS.getOS())
        {
            default:
                log.severe("Something went really wrong while fetching the Hydra directory.");
                return null;
            case Unknown:
                log.warning("Unknown operating system detected. Assuming Linux.");
                return new File(getLinuxHomeDirectory() + "/.hydra");
            case Windows:
                return new File(System.getProperty("user.home") + "/AppData/Roaming/.hydra");
            case MacOS:
                return new File("~/Library/Application Support/.hydra");
            case Linux:
                return new File(getLinuxHomeDirectory() + "/.hydra");
        }
    }

    public static File getLibrariesDirectory()
    {
        return new File(getHydraDirectory(), "libraries");
    }

    public static File getVersionsDirectory()
    {
        return new File(getHydraDirectory(), "versions");
    }

    public static File getJavaDirectory()
    {
        return new File(getHydraDirectory(), "jdk");
    }

    public static void alert(String title, String message, Alert.AlertType type)
    {
        Platform.runLater(() ->
        {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.show();
        });
    }

    public static String calcSHA1(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }

    public enum OS
    {
        Windows,
        MacOS,
        Linux,
        Unknown;

        public static OS getOS()
        {
            String flag = System.getProperty("os.name").toLowerCase();
            if (flag.contains("win")) return OS.Windows;
            if (flag.contains("osx") || flag.contains("mac")) return OS.MacOS;
            if (flag.contains("nix") || flag.contains("nux")) return OS.Linux;
            return OS.Unknown;
        }
    }
}
