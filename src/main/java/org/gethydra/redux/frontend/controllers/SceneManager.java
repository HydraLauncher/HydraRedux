package org.gethydra.redux.frontend.controllers;

import javafx.stage.Stage;
import org.gethydra.redux.frontend.controllers.scenes.GameOutputScene;
import org.gethydra.redux.frontend.controllers.scenes.MainScene;
import org.gethydra.redux.frontend.controllers.scenes.ProfileEditorScene;

import java.util.HashMap;
import java.util.logging.Logger;

public class SceneManager
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private HashMap<String, HydraScene<?>> scenes;

    private Stage primaryStage;
    private HydraScene currentScene;

    public SceneManager(Stage primaryStage)
    {
        scenes = new HashMap<>();

        if (primaryStage == null)
            log.severe("Failed to create scene manager: primaryStage == null");

        this.primaryStage = primaryStage;
    }

    public void loadScenes()
    {
        scenes.put("Login", new HydraScene<>("/assets/fxml/Login.fxml"));
        scenes.put("Main", new MainScene());
        scenes.put("News", new HydraScene<>("/assets/fxml/News.fxml"));
        scenes.put("ServerList", new HydraScene<>("/assets/fxml/ServerList.fxml"));
        scenes.put("Mods", new HydraScene<>("/assets/fxml/Mods.fxml"));
        scenes.put("GameOutput", new GameOutputScene());
        scenes.put("ProfileEditor", new ProfileEditorScene());
        scenes.put("Error", new HydraScene<>("/assets/fxml/Error.fxml"));

        log.info("Scenes have been loaded!");
    }

    public Stage getPrimaryStage()
    {
        return primaryStage;
    }

    public HydraScene<?> getCurrentScene()
    {
        return currentScene;
    }

    public <T> HydraScene<T> getScene(String name)
    {
        return (HydraScene<T>) scenes.get(name);
    }

    public void setScene(HydraScene<?> scene)
    {
        this.currentScene = scene;
        if (scene == null)
            log.severe("setScene failed: scene == null");
        if (this.primaryStage == null)
            log.severe("setScene failed: primaryStage == null");
        if (scene != null && scene.getScene() == null)
            log.severe("setScene failed: scene.getScene() == null");
        assert scene != null;
        this.primaryStage.setScene(scene.getScene());
        scene.fireSceneSetEvent();
    }

    public void show()
    {
        primaryStage.show();
    }

    public void hide()
    {
        primaryStage.hide();
    }
}
