package org.gethydra.redux;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.gethydra.redux.backend.CrashReport;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.JavaManager;
import org.gethydra.redux.backend.auth.LastLoginInfo;
import org.gethydra.redux.backend.auth.MethodManager;
import org.gethydra.redux.backend.auth.SessionValidateRequest;
import org.gethydra.redux.backend.download.DownloadManager;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.profiles.ProfileManager;
import org.gethydra.redux.backend.versions.*;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
import org.gethydra.redux.frontend.controllers.SceneManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class HydraRedux
{
    private static final Gson gson = new Gson();
    private static Logger log = Logger.getLogger("HydraRedux");
    private static HydraRedux instance = null;
    private static final HttpClient client = HttpClientBuilder.create().build();

    private SceneManager sceneManager;
    private DataStore dataStore;
    private MethodManager methodManager;
    private JavaManager javaManager;
    private DownloadManager downloadManager;
    private ProfileManager profileManager;
    private double xOffset, yOffset;
    private BJManifest versionManifest;

    protected void init(Stage primaryStage) throws Exception
    {
        instance = this;

        String vman_json = Util.get("https://mcphackers.org/BetterJSONs/version_manifest.json");
        //TODO: load built-in backup in case this request fails
        this.versionManifest = gson.fromJson(vman_json, BJManifest.class);

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
            this.profileManager.setSelectedProfile((!this.profileManager.getProfiles().isEmpty()) ? this.profileManager.getProfiles().get(0) : this.profileManager.createNewProfile("Default"));

        String startingScene = "Login";
        LastLoginInfo lastLogin = hasValidSession();
        if (lastLogin != null)
        {
            DataStore store = HydraRedux.getInstance().getDataStore();
            store.setString("username", lastLogin.username);
            store.setString("accessToken", lastLogin.accessToken);
            startingScene = "Main";

            log.info("Found a valid Hydra session in lastlogin.json");
        } else log.info("No valid Hydra session found on disk, prompting for login");

        sceneManager.getPrimaryStage().initStyle(StageStyle.TRANSPARENT);
        sceneManager.getPrimaryStage().getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/favicon.png"))));
        sceneManager.getPrimaryStage().setTitle("Hydra");
        sceneManager.getPrimaryStage().setResizable(false);
        sceneManager.setScene(sceneManager.getScene(startingScene));
        sceneManager.show();

        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
        {
            e.printStackTrace(System.err);

            CrashReport report = new CrashReport(System.getProperty("os.name"), System.getProperty("java.version"));
            for (StackTraceElement stackTraceElement : e.getStackTrace())
                report.addStackTraceElement(stackTraceElement);
            //TODO: report crash?
            //sceneManager.setScene(sceneManager.<org.gethydra.redux.frontend.controllers.Error>getScene("Error"));
        });
    }

    public LastLoginInfo hasValidSession()
    {
        File lastLoginFile = new File(Util.getHydraDirectory(), "lastlogin.json");
        if (lastLoginFile.exists())
        {
            try (JsonReader reader = new JsonReader(new FileReader(lastLoginFile)))
            {
                LastLoginInfo lastLogin = gson.fromJson(reader, LastLoginInfo.class);

                HttpPost post = new HttpPost(new URI("https://gethydra.org/api/v1/validate"));
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Authorization", "Bearer " + lastLogin.accessToken);
                HttpResponse response = client.execute(post);

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) return lastLogin;
                else if (statusCode == 401) log.info("Existing session data could not be reused: API says it's invalid");
                else log.info("API returned unexpected status code: " + statusCode);
                return null;
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                return null;
            }
        } else return null;
    }

    public void logout()
    {
        File lastLoginFile = new File(Util.getHydraDirectory(), "lastlogin.json");
        if (!lastLoginFile.delete()) lastLoginFile.deleteOnExit();
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

    public BJManifest getVersionManifest()
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
