package org.gethydra.redux.backend;

import org.gethydra.redux.Util;
import org.gethydra.redux.backend.download.Download;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class JavaManager
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private ArrayList<JavaInstallation> installations;

    public JavaManager()
    {
        this.installations = new ArrayList<>();
    }

    public void clearAndLocate()
    {
        installations.clear();
        //TODO: find all downloaded installations
        for (File dir : Util.getJavaDirectory().listFiles())
        {
            File binDir = new File(dir, "bin");
            if (dir.isDirectory() && new File(binDir, "java.exe").exists())
            {
                String version = binDir.getPath().split("-")[1];
                JavaVersion jv = JavaVersion.find(version);
                if (jv != null)
                {
                    registerInstallation(jv.constructInstallation());
                    log.warning("Found Java installation: " + version);
                }
            }
        }
    }

    public void registerInstallation(JavaInstallation installation)
    {
        installations.add(installation);
    }

    public JavaInstallation getInstallation(String version)
    {
        for (JavaInstallation inst : installations)
            if (inst.version.equalsIgnoreCase(version))
                return inst;
        return null;
    }

    public Download createDownload(JavaVersion version) throws IOException
    {
        try
        {
            String jdkFileName = String.format("jdk-%s-%s-%s.%s", version.version, version.platform, version.architecture, version.extension);
            File outputFile = new File(Util.getJavaDirectory(), jdkFileName);

            Download download = new Download(String.format("https://cdn.gethydra.org/cdn/jdk/%s", jdkFileName), outputFile.getAbsolutePath(), "yeet", true);
            return download;

            //installations.add(new JavaInstallation(version.version, new File(outputDir, "bin").getAbsolutePath()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static class JavaInstallation
    {
        public String version;
        public String binPath;

        public JavaInstallation() {}

        public JavaInstallation(String version, String binPath)
        {
            this.version = version;
            this.binPath = binPath;
        }

        public File getJavaExecutable()
        {
            File bin = new File(binPath);
            return new File(bin, "java.exe");
        }
    }

    public enum JavaVersion
    {
        JAVA_8_WINDOWS("8", Util.OS.Windows, "x64"),
        JAVA_9_WINDOWS("9", Util.OS.Windows, "x64"),
        JAVA_10_WINDOWS("10", Util.OS.Windows, "x64"),
        JAVA_11_WINDOWS("11", Util.OS.Windows, "x64"),
        JAVA_12_WINDOWS("12", Util.OS.Windows, "x64"),
        JAVA_13_WINDOWS("13", Util.OS.Windows, "x64"),
        JAVA_14_WINDOWS("14", Util.OS.Windows, "x64"),
        JAVA_15_WINDOWS("15", Util.OS.Windows, "x64"),
        JAVA_16_WINDOWS("16", Util.OS.Windows, "x64");

        JavaVersion(String version, Util.OS platform, String architecture)
        {
            this.version = version;
            this.platform = platform.toString().toLowerCase();
            this.architecture = architecture;
            switch (platform)
            {
                case Windows:
                    this.extension = "zip";
                    break;
                default:
                    this.extension = "tar.gz";
            }
        }

        public JavaInstallation constructInstallation()
        {
            File jdkDir = new File(Util.getJavaDirectory(), String.format("jdk-%s-%s-%s", this.version, this.platform, this.architecture));
            File jdkBinDir = new File(jdkDir, "bin");
            return new JavaInstallation(this.version, jdkBinDir.getAbsolutePath());
        }

        public static JavaVersion find(String version)
        {
            for (JavaVersion v : values())
                if (v.version.equalsIgnoreCase(version))
                    return v;
            return null;
        }

        public final String version;
        public final String platform;
        public final String architecture;
        public final String extension;
    }
}
