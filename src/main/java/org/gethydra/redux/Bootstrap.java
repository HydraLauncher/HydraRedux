package org.gethydra.redux;

import org.gethydra.redux.backend.ZipUtil;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;

public class Bootstrap
{
    public static void main(String[] args)
    {
        // ensure hydra directory exists
        Util.getHydraDirectory().mkdirs();

        File currentJar = null;
        try
        {
            currentJar = new File(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!currentJar.getName().endsWith(".jar")) // tf am i supposed to do without a jar?
            {
                JOptionPane.showMessageDialog(null, "Runtime code-source URI location does not point to a JAR file. Contact developers for assistance.");
                System.exit(1);
                return;
            }

            extractJavaFX();

            String javaExecutable = "java"; // fallback command
            switch (Util.OS.getOS())
            {
                default:
                case Unknown:
                case Windows:
                    javaExecutable = findJavaWindows();
                    break;
                case OSX:
                case Linux:
                    javaExecutable = findJavaUnix();
                    break;
            }

            if (javaExecutable == null || !new File(javaExecutable).exists())
            {
                JOptionPane.showMessageDialog(null, "Hydra was unable to locate a Java installation.\nIt will attempt to continue, but if the launcher does not open then\nplease contact the developers for assistance.");
                javaExecutable = "java";
            }

            ProcessBuilder pb = new ProcessBuilder(
                    javaExecutable,
                    "-Dprism.lcdtext=false",
                    "--module-path",
                    new File(Util.getHydraDirectory(), "fx-runtime/lib/").getAbsolutePath(),
                    "--add-modules",
                    "javafx.controls,javafx.fxml,javafx.graphics,javafx.swing",
                    "-cp",
                    currentJar.getAbsolutePath(),
                    "org.gethydra.redux.Main"
            );

            pb.start();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private static void extractJavaFX()
    {
        File tmp = new File(Util.getHydraDirectory(), "temp/");
        File runtimeFile = new File(Util.getHydraDirectory(), "temp/fx-runtime.zip");
        File runtimeDst = new File(Util.getHydraDirectory(), "fx-runtime/");
        if (new File(runtimeDst, "javafx.base.jar").exists()) return; // simple check to see if runtime already exists
        tmp.mkdirs();
        runtimeFile.getParentFile().mkdirs();
        runtimeDst.mkdirs();

        try (InputStream is = Bootstrap.class.getResourceAsStream("/fx-runtime-" + Util.OS.getOS().toString().toLowerCase() + ".zip"))
        {
            assert is != null;
            Files.copy(is, runtimeFile.toPath());
            ZipUtil.extractAllTo(runtimeFile.getAbsolutePath(), runtimeDst.getAbsolutePath());
            if (!runtimeFile.delete()) runtimeFile.deleteOnExit();
            if (!tmp.delete()) tmp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private static String findJavaWindows()
    {
        String[] pathDirs = System.getenv("PATH").split(";");
        for (String pathDir : pathDirs)
        {
            File javaFile = new File(pathDir, "java.exe");
            if (javaFile.exists()) return javaFile.getAbsolutePath();
        }
        return null;
    }

    private static String findJavaUnix()
    {
        try
        {
            Process process = Runtime.getRuntime().exec("whereis -b java");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String latestJavaPath = null;

            while ((line = reader.readLine()) != null)
            {
                String[] locations = line.split(":");
                if (locations.length > 1)
                {
                    // Use the first location returned by 'whereis'
                    latestJavaPath = locations[1].trim();
                    // Follow symlinks to get the actual location
                    latestJavaPath = getRealPath(latestJavaPath);
                    break;
                }
            }

            process.waitFor();
            if (latestJavaPath != null) return latestJavaPath;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return null;
    }

    private static String getRealPath(String path)
    {
        try
        {
            Process process = Runtime.getRuntime().exec("realpath " + path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String realPath = reader.readLine();
            process.waitFor();
            return realPath;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return path; // Return the original path if 'realpath' command fails
    }
}
