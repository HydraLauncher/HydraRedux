package org.gethydra.redux.backend.launch;

import jdk.jshell.spi.ExecutionControl;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.MinecraftServerAddress;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.JavaManager;
import org.gethydra.redux.backend.TokenProcessor;
import org.gethydra.redux.backend.ZipUtil;
import org.gethydra.redux.backend.download.Download;
import org.gethydra.redux.backend.download.DownloadManager;
import org.gethydra.redux.backend.download.DownloadTracker;
import org.gethydra.redux.backend.download.HydraEvent;
import org.gethydra.redux.backend.mods.Mod;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.versions.Artifact;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.Library;
import org.gethydra.redux.frontend.controllers.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class LaunchUtility
{
    private static Logger log = Logger.getLogger("HydraRedux");

    public void launch(final Version version, DownloadTracker tracker)
    {
        launch(version, tracker, null);
    }

    public void launch(final Version version, DownloadTracker tracker, MinecraftServerAddress address)
    {
        try
        {
            log.info("Launching game...");

            if (version == null)
            {
                log.severe("Failed to launch: version == null");
                tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_FAILED);
                return;
            }

            LauncherProfile profile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
            DownloadManager downloadManager = HydraRedux.getInstance().getDownloadManager();
            File gameDirectory = new File(profile.getGameDirectory());

            // ensure java is setup correctly
            JavaManager javaManager = HydraRedux.getInstance().getJavaManager();
            JavaManager.JavaInstallation java = javaManager.getInstallation(version.java_version);
            if (java == null) // java isn't installed, queue a download.
                downloadManager.queueDownload(javaManager.createDownload(JavaManager.JavaVersion.find(version.java_version)));

            downloadManager.queueDownload(new Download(version.client.url, new File(Util.getVersionsDirectory(), version.client.path).getAbsolutePath(), version.client.sha1));

            for (Library dep : version.libraries)
            {
                if (dep.downloads != null)
                    for (Artifact artifact : dep.downloads.values())
                        if (artifact != null)
                            downloadManager.queueDownload(new Download(artifact.url, new File(Util.getLibrariesDirectory(), artifact.path).getAbsolutePath(), artifact.sha1));
                if (dep.classifiers != null)
                    for (Artifact artifact : dep.classifiers.values())
                        if (artifact != null)
                            downloadManager.queueDownload(new Download(artifact.url, new File(Util.getLibrariesDirectory(), artifact.path).getAbsolutePath(), artifact.sha1));
            }

            tracker.setTotalBytes(downloadManager.getTotalBytes());
            tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_STARTED);

            while (downloadManager.hasNext())
            {
                try
                {
                    downloadManager.getNext().execute(tracker);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_FAILED);
                    return;
                }
            }

            // if java is null, it means it isn't installed. it will have already been downloaded, so we pretty much just need to extract & register it.
            if (java == null)
            {
                JavaManager.JavaVersion javaVersion = JavaManager.JavaVersion.find(version.java_version);
                assert javaVersion != null;
                String jdkFileName = String.format("jdk-%s-%s-%s.%s", javaVersion.version, javaVersion.platform, javaVersion.architecture, javaVersion.extension);
                File outputFile = new File(Util.getJavaDirectory(), jdkFileName);
                File outputDir = new File(Util.getJavaDirectory(), jdkFileName.split("\\.")[0]);
                ZipUtil.extractAllTo(outputFile.getAbsolutePath(), outputDir.getAbsolutePath());
                if (!outputFile.delete())
                    outputFile.deleteOnExit();
                javaManager.registerInstallation(new JavaManager.JavaInstallation(javaVersion.version, new File(outputDir, "bin").getAbsolutePath()));
                java = javaManager.getInstallation(javaVersion.version);
            }

            cleanupNatives(version);

            File nativesDir = new File(version.getDirectory(), "natives-" + (long) (Math.random() * 100000000000000L));

            // unpack natives
            for (Library lib : version.libraries)
            {
                String artifactName = "natives-" + Util.OS.getOS().name().toLowerCase();
                Artifact artifact = lib.classifiers.get(artifactName);
                if (lib.classifiers.containsKey(artifactName) && new File(Util.getLibrariesDirectory(), artifact.path).exists())
                    ZipUtil.extractAllTo(new File(Util.getLibrariesDirectory(), artifact.path).getAbsolutePath(), nativesDir.getAbsolutePath());
            }

            File tempDir = new File(version.getDirectory(), "tmp");
            File moddedFile = new File(Util.getVersionsDirectory(), String.format("%s/%s-modded.jar", version.id, version.id));
            File clientFile = new File(Util.getVersionsDirectory(), String.format("%s/%s.jar", version.id, version.id));
            ArrayList<Mod> enabledMods = profile.getModManager().getEnabledMods();
            boolean modded = enabledMods.size() > 0;
            int modsInstalled = enabledMods.size();

            if (modded)
            {
                // unzip client to tmp directory
                ZipUtil.extractAllTo(clientFile.getAbsolutePath(), tempDir.getAbsolutePath());
                // unpack all the mods to the tmp directory, overwriting the vanilla files we extracted before
                for (Mod mod : profile.getModManager().getMods())
                    if (mod.isEnabled())
                        ZipUtil.extractAllTo(mod.getFile().getAbsolutePath(), tempDir.getAbsolutePath());
                // the META-INF step is very important (i think)
                File META_INF = new File(tempDir, "META-INF");
                // delete sub files/directories so we'll be able to delete the parent (META-INF)
                for (File file : META_INF.listFiles()) file.delete();
                // delete META-INF
                META_INF.delete();
                // zip the tempDir up into {version}-modded.jar. this will be used instead of the normal {version}.jar
                ZipUtil.zipAllTo(tempDir.getAbsolutePath(), moddedFile.getAbsolutePath());
            }

            log.warning("Mod(s) installed: " + modsInstalled);

            try
            {
                String osFileName = Util.OS.getOS() == Util.OS.Windows ? "java.exe" : "java";
                File javaFile = new File(java.binPath, osFileName);
                if (!javaFile.exists())
                    log.severe("java executable doesn't exist: " + javaFile.getAbsolutePath());
                // build the process & start the game
                String nativesArg = "-Djava.library.path=" + nativesDir.getAbsolutePath();
                String backupNativesArg = "-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder(javaFile.getAbsolutePath(), nativesArg, backupNativesArg, "-classpath", buildClasspath(version, modded ? moddedFile : clientFile), version.main_class, new TokenProcessor(version.arguments).process(), address != null ? address.full() : "");
                gameDirectory.mkdirs();
                pb.directory(gameDirectory);
                pb.inheritIO();
                pb.redirectErrorStream(true); // hack to get proc.waitFor() to return
                log.info("Launch command: " + pb.command().toString());
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setVisible(false); // this kind of static access isn't ideal TODO: fix
                Process proc = pb.start();

                proc.getInputStream().close();
                proc.getErrorStream().close();
                log.warning("Spawned game process.");
                tracker.getEventHandler().fire(HydraEvent.GAME_STARTED);

                // proc.waitFor() won't return if the input stream isn't being read
//                while ((new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine()) != null) {}
//                while ((new BufferedReader(new InputStreamReader(proc.getErrorStream())).readLine()) != null) {}

                // game has stopped. fire event & cleanup.
                while (proc.isAlive()) {}
                System.out.println("Game closed: " + proc.exitValue());
                tracker.setExitCode(proc.exitValue());
                tracker.getEventHandler().fire(HydraEvent.GAME_CLOSED);
                cleanupNatives(version);
                deleteFile(moddedFile);
                deleteDirectory(tempDir);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            tracker.setExitCode(-1);
            tracker.getEventHandler().fire(HydraEvent.GAME_CLOSED);
        }
    }

    private void cleanupNatives(Version version)
    {
        // cleanup any natives from previous sessions
        for (File dir : Objects.requireNonNull(version.getDirectory().listFiles()))
            if (dir.isDirectory() && dir.getName().contains("natives"))
                deleteDirectory(dir);
    }

    private void deleteFile(File file)
    {
        if (!file.delete()) file.deleteOnExit();
    }

    private void deleteDirectory(File dir)
    {
        if (dir.isDirectory())
        {
            for (File f : dir.listFiles())
            {
                if (f.isDirectory())
                    deleteDirectory(f);
                else
                    deleteFile(f);
            }
        }
        deleteFile(dir);
    }

    private String buildClasspath(final Version version, File client)
    {
        StringBuilder sb = new StringBuilder();
        for (Library dep : version.libraries)
            if (dep.downloads.size() > 0) sb.append(new File(Util.getLibrariesDirectory(), dep.downloads.get("artifact").path)).append((Util.OS.getOS() == Util.OS.Windows) ? ";" : ":");
        sb.append(client.getAbsolutePath());
        String cp = sb.toString();
        log.info("Classpath: " + cp);
        return cp;
    }
}
