package org.gethydra.redux.frontend.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.servers.Server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Objects;

public class ServerList extends HydraController
{
    @FXML public Button btnRefresh;
    @FXML public VBox listContainer;

    private org.gethydra.redux.backend.servers.ServerList serverList;

    @FXML protected void initialize()
    {
        refresh();

        setupButtonAnimation(btnRefresh);

        btnRefresh.setOnAction((e) -> refresh());
    }

    public void refresh()
    {
        try
        {
            clearServers();

            serverList = org.gethydra.redux.backend.servers.ServerList.fetch();
            assert serverList != null;
            serverList.getServers().sort((o1, o2) -> o2.onlinePlayers - o1.onlinePlayers);

            if (serverList.getServers().isEmpty())
            {
                SceneManager sceneManager = HydraRedux.getInstance().getSceneManager();
                sceneManager.setScene(sceneManager.<Error>getScene("Error"));
                return;
            }

            for (Server server : serverList.getServers())
            {
                if (!server.isOfficial()) continue;
                addServer(server);
            }

            for (Server server : serverList.getServers())
            {
                if (server.isOfficial()) continue;
                addServer(server);
            }

            try
            {
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setTab(HydraRedux.getInstance().getSceneManager().<ServerList>getScene("ServerList").getController().background);
            } catch (Exception ignored) {}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearServers()
    {
        if (serverList != null)
            serverList.getServers().clear();
        listContainer.getChildren().clear();
    }

    private void addServer(Server server)
    {
        try
        {
            HydraScene<ServerListEntry> entry = new HydraScene<>("/assets/fxml/ServerListEntry.fxml");

            entry.getController().lblServerName.setText(server.serverName);
            entry.getController().lblServerDescription.setText(server.serverDescription);
            entry.getController().lblServerVersion.setText(server.serverVersion.toLowerCase());
            entry.getController().lblPlayerCount.setText(String.format("%s (%s/%s players online)", server.serverIP, server.onlinePlayers, server.maxPlayers));
            entry.getController().verifiedBadge.setVisible(server.isOfficial());
            entry.getController().server = server;

            if (server.serverIcon != null)
            {
                String data = "data:image/png;base64," + server.serverIcon;
                String base64Image = data.split(",")[1];
                byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                entry.getController().icon.setImage(SwingFXUtils.toFXImage(img, null));
            } else {
                entry.getController().icon.setImage(SwingFXUtils.toFXImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/assets/images/pack.jpg"))), null));
            }

            if (listContainer.getChildren().contains(entry.getRoot())) return;
            listContainer.getChildren().add(entry.getRoot());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
