package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.news.NewsEntry;
import org.gethydra.redux.backend.news.NewsManifest;

public class News extends HydraController
{
    @FXML public VBox postContainer;
    @FXML public Button btnRefresh;

    private NewsManifest news;

    public void initialize()
    {
        refresh();

        setupButtonAnimation(btnRefresh);

        btnRefresh.setOnAction((e) -> refresh());
    }

    public void refresh()
    {
        postContainer.getChildren().clear();
        news = NewsManifest.fetch();

        if (news != null)
        {
            for (NewsEntry news : news.getEntries())
            {
                HydraScene<org.gethydra.redux.frontend.controllers.NewsEntry> entry = new HydraScene<>("/assets/fxml/NewsEntry.fxml");
                entry.getController().lnkPost.setText(news.title);
                entry.getController().lineContainer.getChildren().clear();
                for (String line : news.lines)
                    entry.getController().addLine(line);
                int desiredHeight = 32 * news.lines.length;
                entry.getController().lineContainer.setMinHeight(50 + desiredHeight);
                entry.getController().background.setMinHeight(50 + desiredHeight);
                postContainer.getChildren().add(entry.getRoot());
            }

            try
            {
                HydraRedux.getInstance().getSceneManager().<Main>getScene("Main").getController().setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background);
            } catch (Exception ignored) {}
        }
    }
}
