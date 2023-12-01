package org.gethydra.redux.backend.launch;

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
import org.gethydra.redux.backend.versions.betterjsons.BJMinecraftVersion;
import org.gethydra.redux.frontend.controllers.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class LaunchUtility
{
    private static Logger log = Logger.getLogger("HydraRedux");

    public void launch(final BJMinecraftVersion version, DownloadTracker tracker)
    {
        launch(version, tracker, null);
    }

    public void launch(final BJMinecraftVersion version, DownloadTracker tracker, MinecraftServerAddress address)
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
            File versionDir = new File(Util.getVersionsDirectory(), version.id);
            if (!versionDir.exists()) versionDir.mkdirs();

            // ensure java is setup correctly
            JavaManager javaManager = HydraRedux.getInstance().getJavaManager();
            JavaManager.JavaInstallation java = javaManager.getInstallation(String.valueOf(version.javaVersion.majorVersion));
            if (java == null) // java isn't installed, queue a download.
                downloadManager.queueDownload(javaManager.createDownload(JavaManager.JavaVersion.find(String.valueOf(version.javaVersion.majorVersion))));

            ArrayList<Artifact> nativesArtifacts = new ArrayList<>();

            for (BJMinecraftVersion.Library lib : version.libraries)
            {
                Artifact artifact = lib.downloads.artifact;
                Map<String, Artifact> classifiers = lib.downloads.classifiers;

                // null checks
                // queue download for artifact, classifiers, natives, etc
                if (artifact != null)
                {
                    if (artifact.path.contains("natives-" + Util.OS.getOS().toString().toLowerCase()))
                        nativesArtifacts.add(artifact);
                    downloadManager.queueDownload(new Download(artifact.url, new File(Util.getLibrariesDirectory(), artifact.path).getAbsolutePath(), artifact.sha1, true));
                }
                if (classifiers != null)
                {
                    if (lib.natives != null)
                    {
                        String classifierKey = "natives-" + Util.OS.getOS().toString().toLowerCase();
                        Artifact natives = classifiers.get(classifierKey);
                        nativesArtifacts.add(natives);
                        downloadManager.queueDownload(new Download(natives.url, new File(Util.getLibrariesDirectory(), natives.path).getAbsolutePath(), natives.sha1, true));
                    } else {
                        for (Artifact classifier : classifiers.values())
                            if (!classifier.path.contains("sources")) downloadManager.queueDownload(new Download(classifier.url, new File(Util.getLibrariesDirectory(), classifier.path).getAbsolutePath(), classifier.sha1, true));
                    }
                }

                //TODO: perform 'extractions' if any (is this even important? idk)
            }

            HydraRedux.getInstance().getDataStore().setString("assetsDirectory", new File(profile.getGameDirectory(), "resources").getAbsolutePath());
            HydraRedux.getInstance().getDataStore().setString("gameDirectory", new File(profile.getGameDirectory()).getAbsolutePath());

            // download client
            BJMinecraftVersion.PackageDownloads.PackageArtifact client = version.downloads.client;
            downloadManager.queueDownload(new Download(client.url, new File(versionDir, version.id + ".jar").getAbsolutePath(), client.sha1));

            // setup download tracker
            tracker.setTotalBytes(downloadManager.getTotalBytes());
            tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_STARTED);

            // execute downloads
            while (downloadManager.hasNext())
            {
                try
                {
                    downloadManager.getNext().execute(tracker);
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                    tracker.getEventHandler().fire(HydraEvent.DOWNLOAD_FAILED);
                    return;
                }
            }

            if (java == null)
            {
                JavaManager.JavaVersion javaVersion = JavaManager.JavaVersion.find(String.valueOf(version.javaVersion.majorVersion));
                String jdkFileName = String.format("jdk-%s-%s-%s.%s", Objects.requireNonNull(javaVersion).version, javaVersion.platform, javaVersion.architecture, javaVersion.extension);
                File outputFile = new File(Util.getJavaDirectory(), jdkFileName);
                File outputDir = new File(Util.getJavaDirectory(), jdkFileName.split("\\.")[0]);
                ZipUtil.extractAllTo(outputFile.getAbsolutePath(), outputDir.getAbsolutePath());
                if (!outputFile.delete())
                    outputFile.deleteOnExit();
                javaManager.registerInstallation(new JavaManager.JavaInstallation(javaVersion.version, new File(outputDir, "bin").getAbsolutePath()));
                java = javaManager.getInstallation(javaVersion.version);
            }

            // cleanup natives
            for (File dir : Objects.requireNonNull(versionDir.listFiles()))
                if (dir.isDirectory() && dir.getName().contains("natives"))
                    deleteDirectory(dir);

            File nativesDir = new File(versionDir, "natives-" + (long) (Math.random() * 100000000000000L));

            // unpack natives
            if (nativesArtifacts.isEmpty()) log.warning("No natives were found, launch will be sure to fail!");
            for (Artifact natives : nativesArtifacts)
            {
                log.info("Unpacking natives: " + natives.path);
                ZipUtil.extractAllTo(new File(Util.getLibrariesDirectory(), natives.path).getAbsolutePath(), nativesDir.getAbsolutePath());
            }

            // extract minecraft & apply mods, if any are installed
            File tempDir = new File(versionDir, "tmp");
            File moddedFile = new File(Util.getVersionsDirectory(), String.format("%s/%s-modded.jar", version.id, version.id));
            File clientFile = new File(Util.getVersionsDirectory(), String.format("%s/%s.jar", version.id, version.id));

            if (!clientFile.exists())
                log.severe("Client file is missing!");

            ArrayList<Mod> enabledMods = profile.getModManager().getEnabledMods();
            boolean modded = !enabledMods.isEmpty();
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
                if (META_INF.exists()) for (File file : Objects.requireNonNull(META_INF.listFiles())) file.delete();
                // delete META-INF
                META_INF.delete();
                // zip the tempDir up into {version}-modded.jar. this will be used instead of the normal {version}.jar
                ZipUtil.zipAllTo(tempDir.getAbsolutePath(), moddedFile.getAbsolutePath());
            }

            log.warning("Mod(s) installed: " + modsInstalled);

            // attempt to launch the game

            try
            {
                String osFileName = Util.OS.getOS() == Util.OS.Windows ? "java.exe" : "java";
                File javaFile = new File(java.binPath, osFileName);
                if (!javaFile.exists())
                    log.severe("java executable doesn't exist: " + javaFile.getAbsolutePath());
                // build the process & start the game
                String nativesArg = "-Djava.library.path=" + nativesDir.getAbsolutePath();
                String backupNativesArg = "-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath();
                List<Object> pbArgs = new ArrayList<>(List.of(javaFile.getAbsolutePath(), nativesArg, backupNativesArg, "-classpath", buildClasspath(version, modded ? moddedFile : clientFile), version.mainClass));
                pbArgs.addAll(Arrays.asList(new TokenProcessor(version.minecraftArguments).process()));
                if (address != null)
                {
                    pbArgs.add("--server");
                    pbArgs.add(address.hostname);
                    pbArgs.add("--port");
                    pbArgs.add(Integer.toString(address.port));
                }
                String[] stringArray = pbArgs.toArray(new String[0]);
                ProcessBuilder pb = new ProcessBuilder(stringArray);
                gameDirectory.mkdirs();
                pb.directory(gameDirectory);
                pb.inheritIO();
                pb.redirectErrorStream(true); // hack to get proc.waitFor() to return
                log.info("Raw launch cmd array: " + pb.command());
                log.info("Executable launch command: " + parseCommand(pb.command().toString()));
                //HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setVisible(false); // this kind of static access isn't ideal TODO: fix
                Process proc = pb.start();

                proc.getInputStream().close();
                proc.getErrorStream().close();
                log.warning("Spawned game process.");
                tracker.getEventHandler().fire(HydraEvent.GAME_STARTED);

                // game has stopped. fire event & cleanup.
                while (proc.isAlive()) {}
                System.out.println("Game closed: " + proc.exitValue());
                tracker.setExitCode(proc.exitValue());
                tracker.getEventHandler().fire(HydraEvent.GAME_CLOSED);
                // cleanup natives
                for (File dir : Objects.requireNonNull(versionDir.listFiles()))
                    if (dir.isDirectory() && dir.getName().contains("natives"))
                        deleteDirectory(dir);
                deleteFile(moddedFile);
                deleteDirectory(tempDir);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
            // ensure event handler is fired
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            tracker.setExitCode(-1);
            tracker.getEventHandler().fire(HydraEvent.GAME_CLOSED);
        }
    }

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
                ProcessBuilder pb = new ProcessBuilder(javaFile.getAbsolutePath(), nativesArg, backupNativesArg, "-classpath", buildClasspath(version, modded ? moddedFile : clientFile), version.main_class, "", address != null ? address.full() : "");
                gameDirectory.mkdirs();
                pb.directory(gameDirectory);
                pb.inheritIO();
                pb.redirectErrorStream(true); // hack to get proc.waitFor() to return
                log.info("Launch command: " + pb.command().toString());
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setVisible(false); // this kind of static access isn't ideal TODO: fix
                Process proc = pb.start();

                log.warning("Spawned game process.");
                tracker.getEventHandler().fire(HydraEvent.GAME_STARTED);

                // proc.waitFor() won't return if the input stream isn't being read
                while ((new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine()) != null) {}
                String line;
                while ((line = new BufferedReader(new InputStreamReader(proc.getErrorStream())).readLine()) != null)
                {
                    System.err.println(line);
                }

                proc.waitFor();

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
            for (File f : Objects.requireNonNull(dir.listFiles()))
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
            if (!dep.downloads.isEmpty()) sb.append(new File(Util.getLibrariesDirectory(), dep.downloads.get("artifact").path)).append((Util.OS.getOS() == Util.OS.Windows) ? ";" : ":");
        sb.append(client.getAbsolutePath());
        String cp = sb.toString();
        log.info("Classpath: " + cp);
        return cp;
    }

    private String buildClasspath(final BJMinecraftVersion version, File client)
    {
        StringBuilder sb = new StringBuilder();
        for (BJMinecraftVersion.Library lib : version.libraries)
        {
            if (lib.downloads.artifact == null) continue;
            File artifactFile = new File(Util.getLibrariesDirectory(), lib.downloads.artifact.path);
            if (!artifactFile.exists()) log.severe("Missing classpath artifact: " + artifactFile.getAbsolutePath());
            sb.append(artifactFile.getAbsolutePath()).append((Util.OS.getOS() == Util.OS.Windows) ? ";" : ":");
        }
        sb.append(client.getAbsolutePath());
        return sb.toString();
    }

    private String parseCommand(String inputString)
    {
        // Remove the leading "[" and trailing "]" characters
        String trimmedString = inputString.substring(1, inputString.length() - 2);

        // Split the string by commas outside of square brackets
        String[] commandArray = trimmedString.split(",(?![^\\[]*\\])");

        StringBuilder sb = new StringBuilder();

        // Trim whitespace from each element in the array, and append it
        for (String s : commandArray) sb.append(s.trim()).append(" ");

        return sb.toString().trim();
    }
}
