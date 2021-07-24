package org.gethydra.redux;

import com.google.gson.Gson;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.gethydra.redux.backend.CrashReport;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.JavaManager;
import org.gethydra.redux.backend.auth.MethodManager;
import org.gethydra.redux.backend.download.DownloadManager;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.profiles.ProfileManager;
import org.gethydra.redux.backend.versions.*;
import org.gethydra.redux.frontend.controllers.SceneManager;

import java.util.Objects;
import java.util.logging.Logger;

public class HydraRedux
{
    private static final Gson gson = new Gson();
    private static Logger log = Logger.getLogger("HydraRedux");
    private static HydraRedux instance = null;

    private SceneManager sceneManager;
    private DataStore dataStore;
    private MethodManager methodManager;
    private JavaManager javaManager;
    private DownloadManager downloadManager;
    private ProfileManager profileManager;
    private double xOffset, yOffset;
    private VersionManifest versionManifest;

    protected void init(Stage primaryStage) throws Exception
    {
        instance = this;

        this.versionManifest = gson.fromJson(Util.get("https://launchermeta.gethydra.org/version_manifest.json"), VersionManifest.class);

        this.sceneManager = new SceneManager(primaryStage);
        this.dataStore = new DataStore();
        this.methodManager = new MethodManager();
        this.javaManager = new JavaManager();
        this.downloadManager = new DownloadManager();
        this.profileManager = new ProfileManager();

        sanity();

        this.profileManager.loadFromDisk();
        this.profileManager.saveToDisk();
        this.javaManager.clearAndLocate();
        this.sceneManager.loadScenes();

        LauncherProfile selectedProfile = this.profileManager.getProfile(this.profileManager.getProfileManifest().selectedProfile);
        if (selectedProfile == null)
            this.profileManager.setSelectedProfile((this.profileManager.getProfiles().size() > 0) ? this.profileManager.getProfiles().get(0) : this.profileManager.createNewProfile("Default"));

        sceneManager.getPrimaryStage().initStyle(StageStyle.TRANSPARENT);
        sceneManager.getPrimaryStage().getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/favicon.png"))));
        sceneManager.getPrimaryStage().setTitle("Hydra");
        sceneManager.getPrimaryStage().setResizable(false);
        sceneManager.setScene(sceneManager.getScene("Login"));
        sceneManager.show();

        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
        {
            e.printStackTrace();

            CrashReport report = new CrashReport(System.getProperty("os.name"), System.getProperty("java.version"));
            for (StackTraceElement stackTraceElement : e.getStackTrace())
                report.addStackTraceElement(stackTraceElement);
            //TODO: report crash?
            //sceneManager.setScene(sceneManager.<org.gethydra.redux.frontend.controllers.Error>getScene("Error"));
        });
    }

    public void logout()
    {
        profileManager.saveToDisk();
        sceneManager.setScene(sceneManager.getScene("Login"));
    }

    private void sanity()
    {
        if (!Util.getHydraDirectory().exists())
            Util.getHydraDirectory().mkdirs();
        if (!Util.getVersionsDirectory().exists())
            Util.getVersionsDirectory().mkdirs();
        if (!Util.getLibrariesDirectory().exists())
            Util.getVersionsDirectory().mkdirs();
        if (!Util.getJavaDirectory().exists())
            Util.getJavaDirectory().mkdirs();
    }

    public ProfileManager getProfileManager()
    {
        return profileManager;
    }

    public SceneManager getSceneManager()
    {
        return sceneManager;
    }

    public DataStore getDataStore()
    {
        return dataStore;
    }

    public MethodManager getMethodManager()
    {
        return methodManager;
    }

    public JavaManager getJavaManager()
    {
        return javaManager;
    }

    public DownloadManager getDownloadManager()
    {
        return downloadManager;
    }

    public VersionManifest getVersionManifest()
    {
        return versionManifest;
    }

    public static void close()
    {
        //TODO: make sure everything is saved, etc
        log.info("Closing");
        System.exit(0);
    }

    public static HydraRedux getInstance()
    {
        return instance;
    }
}
