package org.gethydra.redux.frontend.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.download.DownloadTracker;
import org.gethydra.redux.backend.launch.LaunchUtility;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.profiles.ProfileManager;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
import org.gethydra.redux.event.Events;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class Main extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Circle avatar;

    @FXML public Pane topBar, bottomBar, bottomBarLogo, currentTab, lastPane;

    @FXML public Button btnPlay, btnEditProfile, btnLogout, btnNewProfile, btnDeleteProfile, btnCloseWindow;

    @FXML public Label lblPlay, lblUsername, lblAccountType, lblVersion;
    @FXML public Label tabNews, tabSkins, tabServers, tabMods;

    @FXML public ComboBox<LauncherProfile> cmbProfile;

    @FXML public ProgressBar pBar;

    @FXML public void initialize()
    {
        ProfileManager pm = HydraRedux.getInstance().getProfileManager();
        pm.getEventBus().subscribe((event) ->
        {
            switch (event)
            {
                default:
                    break;
                case PROFILE_ADDED:
                case PROFILE_REMOVED:
                    refreshProfile();
                    break;
            }
        });

        refreshProfile();
        cmbProfile.getSelectionModel().select(HydraRedux.getInstance().getProfileManager().getSelectedProfile());

        setupButtonAnimation(btnPlay, 1.03D);
        setupButtonAnimation(btnNewProfile);
        setupButtonAnimation(btnEditProfile);
        setupButtonAnimation(btnDeleteProfile);
        setupButtonAnimation(btnLogout, 1.08D);
        setupButtonAnimation(btnCloseWindow, 1.08D);

        btnDeleteProfile.setOnAction((e) ->
        {
            try
            {
                if (pm.getProfiles().size() == 1)
                {
                    Util.alert("Oh noes!", "You can't delete your only profile!", Alert.AlertType.ERROR);
                    return;
                }
                pm.removeAndSave(pm.getSelectedProfile().getName());
                LauncherProfile profile = pm.getProfiles().get(0);
                if (profile == null)
                {
                    profile = pm.createNewProfile("Default");
                    pm.addAndSave(profile);
                }
                cmbProfile.getSelectionModel().select(profile);
                HydraRedux.getInstance().getSceneManager().<ProfileEditor>getScene("ProfileEditor").fireSceneShownEvent();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        btnNewProfile.setOnAction((e) ->
        {
            try
            {
                LauncherProfile profile = pm.getProfile("New Profile");
                int num = 1;
                if (profile != null)
                {
                    while (profile != null)
                    {
                        profile = pm.getProfile("New Profile " + num);
                        num++;
                    }
                    profile = pm.createNewProfile("New Profile " + num);
                } else {
                    profile = pm.createNewProfile("New Profile");
                }

                pm.addAndSave(profile);
                cmbProfile.getSelectionModel().select(profile);
                setTab(HydraRedux.getInstance().getSceneManager().<ProfileEditor>getScene("ProfileEditor").getController().background);
                HydraRedux.getInstance().getSceneManager().<ProfileEditor>getScene("ProfileEditor").fireSceneShownEvent();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        cmbProfile.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null) HydraRedux.getInstance().getProfileManager().setSelectedProfile(newValue, false);
            pm.fireEventBus(Events.PROFILE_SELECTED);
        });

        btnPlay.setOnAction(e ->
        {
            try
            {
                DownloadTracker tracker = new DownloadTracker();

                tracker.setStatusHandler((status) ->
                {
                    LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
                    switch (status)
                    {
                        case DOWNLOAD_STARTED:
                            pBar.setVisible(true);
                            break;
                        case DOWNLOAD_TRACKER_UPDATE:
                            pBar.progressProperty().set((double)tracker.getDownloadedBytes() / (double)tracker.getTotalBytes());
                            pBar.getStyleClass().remove(".dl-completed");
                            pBar.getStyleClass().remove(".dl-failed");
                            break;
                        case DOWNLOAD_FAILED:
                            log.severe("Download failed");
                            pBar.setProgress(0.0D);
                            pBar.setVisible(false);
                            setLocked(false);
                            break;
                        case GAME_STARTED:
                            log.warning("Game started");
                            pBar.setVisible(false);
                            switch (selectedProfile.getLauncherVisibility())
                            {
                                default:
                                    break;
                                case CLOSE:
                                    System.exit(0);
                                    break;
                                case KEEP_OPEN:
                                    setLocked(true);
                                    break;
                                case HIDE_THEN_OPEN:
                                    HydraRedux.getInstance().getSceneManager().hide();
                                    break;
                            }
                            //setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background);
                            break;
                        case GAME_CLOSED:
                            log.warning("Game closed with exit code: " + tracker.getExitCode());
                            if (tracker.getExitCode() != 0) Util.alert("Oh noes!", "The game crashed!", Alert.AlertType.ERROR);
                            switch (HydraRedux.getInstance().getProfileManager().getSelectedProfile().getLauncherVisibility())
                            {
                                default:
                                case KEEP_OPEN:
                                    break;
                                case CLOSE:
                                    System.exit(0);
                                    break;
                                case HIDE_THEN_OPEN:
                                    HydraRedux.getInstance().getSceneManager().show();
                                    setLocked(false);
                                    break;
                            }
                    }
                });
                //setLocked(true);
                LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
                String selectedVersionStr = selectedProfile.getSelectedVersion();
                BJManifest.BJVersionEntry selectedVersion = HydraRedux.getInstance().getVersionManifest().find(selectedVersionStr);
                if (selectedVersion == null) throw new RuntimeException("selectedVersion == null");
                new Thread(() -> new LaunchUtility().launch(selectedVersion.fetch(), tracker)).start();
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                //TODO: display error
            }
        });

        btnLogout.setOnAction(e ->
        {
            if (Util.getConfirmation("Wait!", "Logging out will delete your session. Are you sure you want to continue?"))
                HydraRedux.getInstance().logout();
        });
        btnCloseWindow.setOnAction(e -> HydraRedux.close());

        // news tab should be selected by default
        resetTabColors();
        tabNews.getStyleClass().add("selectedTab");
        Platform.runLater(() -> setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background));

        tabNews.setOnMouseClicked((e) ->
        {
            resetTabColors();
            tabNews.getStyleClass().add("selectedTab");
            setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background);
        });

        tabServers.setOnMouseClicked((e) ->
        {
            resetTabColors();
            tabServers.getStyleClass().add("selectedTab");
            setTab(HydraRedux.getInstance().getSceneManager().<ServerList>getScene("ServerList").getController().background);
        });

        tabMods.setOnMouseClicked((e) ->
        {
            resetTabColors();
            tabMods.getStyleClass().add("selectedTab");
            setTab(HydraRedux.getInstance().getSceneManager().<Mods>getScene("Mods").getController().background);
        });

        btnEditProfile.setOnAction((e) ->
        {
            HydraScene<ProfileEditor> peScene = HydraRedux.getInstance().getSceneManager().getScene("ProfileEditor");
            peScene.fireSceneShownEvent();
            setTab(peScene.getController().background);
        });
    }

    public void refreshProfile()
    {
        ProfileManager pm = HydraRedux.getInstance().getProfileManager();
        LauncherProfile selectedProfile = pm.getSelectedProfile();
        cmbProfile.setItems(FXCollections.observableArrayList(pm.getProfiles()));
        cmbProfile.getSelectionModel().select(selectedProfile);
        lblVersion.setText("Ready to play: " + selectedProfile.getSelectedVersion());
    }

    public void setTab(Pane pane)
    {
        lastPane = (currentTab.getChildren().size() > 0) ? (Pane) currentTab.getChildren().get(0) : null;
        // for some reason i have to do this twice to get it to display with one click TODO: fix
        currentTab.getChildren().clear();
        currentTab.getChildren().setAll(pane);
        currentTab.getChildren().clear();
        currentTab.getChildren().setAll(pane);
    }

    public void restoreLastTab()
    {
        if (lastPane == null)
            setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background);
        else
            setTab((Pane) lastPane.getChildren().get(0));
    }

    public void setLocked(boolean flag)
    {
        btnPlay.setDisable(flag);
        btnNewProfile.setDisable(flag);
        btnEditProfile.setDisable(flag);
        btnDeleteProfile.setDisable(flag);
        btnLogout.setDisable(flag);

        cmbProfile.setDisable(flag);

        tabNews.setDisable(flag);
        tabSkins.setDisable(flag);
        tabServers.setDisable(flag);
        tabMods.setDisable(flag);
    }

    private void resetTabColors()
    {
        tabNews.getStyleClass().removeIf((styleClass) -> styleClass.equalsIgnoreCase("selectedTab"));
        tabSkins.getStyleClass().removeIf((styleClass) -> styleClass.equalsIgnoreCase("selectedTab"));
        tabServers.getStyleClass().removeIf((styleClass) -> styleClass.equalsIgnoreCase("selectedTab"));
        tabMods.getStyleClass().removeIf((styleClass) -> styleClass.equalsIgnoreCase("selectedTab"));
    }
}
