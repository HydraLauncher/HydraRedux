package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.MinecraftServerAddress;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.download.DownloadTracker;
import org.gethydra.redux.backend.launch.LaunchUtility;
import org.gethydra.redux.backend.servers.Server;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
import org.gethydra.redux.frontend.FrontendDownloadTracker;

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
                tracker.setStatusHandler(new FrontendDownloadTracker(tracker));
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setLocked(true);
                BJManifest.BJVersionEntry selectedVersion = HydraRedux.getInstance().getVersionManifest().find(lblServerVersion.getText());
                if (selectedVersion == null) throw new RuntimeException("Could not find selected version: " + lblServerVersion.getText());
                new Thread(() -> new LaunchUtility().launch(selectedVersion.fetch(), tracker, new MinecraftServerAddress(server.serverIP, server.serverPort))).start();
            } catch (Exception ex) {
                Util.alert("Oh noes!", "Failed to join the server!" + System.lineSeparator() + "The server is likely reporting an incorrect version to the list API." + System.lineSeparator() + "Contact the server administrators to have them fix this.", Alert.AlertType.ERROR);
            }
        });
    }
}
