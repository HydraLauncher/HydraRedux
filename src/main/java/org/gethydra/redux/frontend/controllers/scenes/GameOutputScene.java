package org.gethydra.redux.frontend.controllers.scenes;

import org.gethydra.redux.frontend.controllers.GameOutput;
import org.gethydra.redux.frontend.controllers.HydraScene;

public class GameOutputScene extends HydraScene<GameOutput>
{
    public GameOutputScene()
    {
        super("/assets/fxml/GameOutput.fxml");

        this.sceneSetEventHandler = () ->
        {
            getController().outputField.clear();
        };
    }
}
