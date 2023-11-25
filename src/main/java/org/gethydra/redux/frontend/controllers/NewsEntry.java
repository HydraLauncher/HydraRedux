package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import org.gethydra.redux.HydraRedux;

public class NewsEntry extends HydraController
{
    @FXML public Hyperlink lnkPost;
    @FXML public VBox lineContainer;

    public void addLine(String line)
    {
        HydraScene<NewsEntryLine> lineEntry = new HydraScene<>("/assets/fxml/NewsEntryLine.fxml");
        lineEntry.getController().lblContent.setText(line.trim());
        lineEntry.getController().lblContent.setPrefWidth(HydraRedux.getInstance().getSceneManager().getPrimaryStage().getMaxWidth());
        lineContainer.getChildren().add(lineEntry.getRoot());
    }
}
