package org.gethydra.redux;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
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
        SwingUtilities.invokeLater(() ->
        {
            if (Desktop.isDesktopSupported())
            {
                try
                {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE))
                        desktop.browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    Util.alert("Oh noes!", "Failed to open link: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    public static String[] parseJavaInstallation(String javaHome)
    {
        File javaHomeDir = new File(javaHome);
        String version = null;
        String architecture = null;

        if (javaHomeDir.isDirectory())
        {
            String[] parts = javaHomeDir.getName().split("[_-]");

            if (parts.length >= 2)
            {
                version = parts[1];
                if (parts.length >= 3) architecture = parts[2];
            }
        }

        if (version != null) return new String[]{version, architecture};
        return null;
    }

    public static boolean isFileSymlink(File file)
    {
        try
        {
            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attributes.isSymbolicLink();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String get(String url)
    {
        try
        {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpPost = new HttpGet(url);
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

    public static boolean getConfirmation(String title, String message)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Confirm Action");
        alert.setContentText(message);
        Optional<ButtonType> option = alert.showAndWait();
        return ButtonType.OK.equals(option.get());
    }

    public static String calcSHA1(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        try (InputStream input = new FileInputStream(file))
        {
            return DigestUtils.sha1Hex(input);
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
