package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.MinecraftServerAddress;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.download.DownloadTracker;
import org.gethydra.redux.backend.launch.LaunchUtility;
import org.gethydra.redux.backend.servers.Server;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;

import java.util.logging.Logger;

public class ServerListEntry extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Pane verifiedBadgeTooltip;
    @FXML public ImageView icon, verifiedBadge;
    @FXML public Label lblServerName, lblServerDescription, lblPlayerCount, lblServerVersion;
    @FXML public Button btnJoin;

    public Server server;

    @FXML protected void initialize()
    {
        verifiedBadge.setOnMouseEntered((e) -> fadeTransition(verifiedBadgeTooltip, 1.0D));
        verifiedBadge.setOnMouseExited((e) -> fadeTransition(verifiedBadgeTooltip, 0.0D));

        btnJoin.setOnMouseEntered((e) -> scaleTransition((Button) e.getSource(), 1.1D));
        btnJoin.setOnMouseExited((e) -> scaleTransition((Button) e.getSource(), 1.0D));

        btnJoin.setOnAction((e) ->
        {
            try
            {
                DataStore store = HydraRedux.getInstance().getDataStore();
                AuthenticatedUser user = (AuthenticatedUser) store.getObject("user", null);
                DownloadTracker tracker = new DownloadTracker();

                tracker.setStatusHandler((status) ->
                {
                    switch (status)
                    {
                        case DOWNLOAD_STARTED:
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setVisible(true);
                            break;
                        case DOWNLOAD_TRACKER_UPDATE:
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.progressProperty().set((double)tracker.getDownloadedBytes() / (double)tracker.getTotalBytes());
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.getStyleClass().remove(".dl-completed");
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.getStyleClass().remove(".dl-failed");
                            break;
                        case DOWNLOAD_COMPLETED:
                            break;
                        case DOWNLOAD_FAILED:
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setProgress(0.0D);
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setLocked(false);
                            break;
                        case GAME_STARTED:
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().pBar.setVisible(false);
                            break;
                        case GAME_CLOSED:
                            HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setLocked(false);
                            break;
                    }
                });
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setLocked(true);
                BJManifest.BJVersionEntry selectedVersion = HydraRedux.getInstance().getVersionManifest().find(lblServerVersion.getText());
                new Thread(() -> new LaunchUtility().launch(selectedVersion.fetch(), tracker, new MinecraftServerAddress(server.serverIP, server.serverPort))).start();
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                //TODO: display error
            }
        });
    }
}
