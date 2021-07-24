package org.gethydra.redux.frontend.controllers.scenes;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.frontend.controllers.HydraScene;
import org.gethydra.redux.frontend.controllers.Main;

public class MainScene extends HydraScene<Main>
{
    private String username;
    private String accessToken;

    public MainScene()
    {
        super("/assets/fxml/Main.fxml");

        this.sceneSetEventHandler = () ->
        {
            DataStore store = HydraRedux.getInstance().getDataStore();
            this.username = store.getString("username", "MHF_Steve");
            this.accessToken = store.getString("accessToken", "1337");

            //TODO: change to conform to account type when those are added in
            String skinUrl = String.format("https://api.gethydra.org/cosmetics/avatar?username=%s&type=hydra", this.username);
            getController().avatar.setFill(new ImagePattern(new Image(skinUrl)));

            getController().lblUsername.setText(username);
            getController().lblAccountType.setText("Hydra Account"); //TODO: change this when account types get added in

            Platform.runLater(() -> getController().cmbProfile.getSelectionModel().select(HydraRedux.getInstance().getProfileManager().getSelectedProfile()));
        };
    }

    private Image crop(Image img, boolean toCircle)
    {
        double d = Math.min(img.getWidth(),img.getHeight());
        double x = (d-img.getWidth())/2;
        double y = (d-img.getHeight())/2;
        Canvas canvas = new Canvas(d, d);
        GraphicsContext g = canvas.getGraphicsContext2D();
        if (toCircle)
        {
            g.fillOval(0, 0, d, d);
            g.setGlobalBlendMode(BlendMode.SRC_ATOP);
        }
        g.drawImage(img, x, y);
        return canvas.snapshot(null, null);
    }
}
