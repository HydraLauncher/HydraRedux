package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.VoidPipe;

import java.io.File;
import java.util.logging.Logger;

public class HydraScene<T>
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private Node root;
    private Scene scene;
    private T controller;
    private double xOffset, yOffset;
    protected VoidPipe sceneSetEventHandler;
    protected VoidPipe sceneShownEventHandler;

    public HydraScene(String source)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(source));
            this.root = loader.load();
            this.scene = new Scene((Parent)root);
            this.controller = loader.getController();

            if (this.root == null)
                log.severe("Failed to create hydra scene: root == null");
            if (this.scene == null)
                log.severe("Failed to create hydra scene: scene == null");
            if (this.controller == null)
                log.severe("Failed to create hydra scene: controller == null");

            this.scene.setFill(Color.TRANSPARENT);

            getRoot().setOnMousePressed(event ->
            {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            getRoot().setOnMouseDragged(event ->
            {
                Stage primaryStage = HydraRedux.getInstance().getSceneManager().getPrimaryStage();
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            });

            File fCustomStyle = new File(Util.getHydraDirectory(), "custom-theme.css");
            if (fCustomStyle.exists())
            {
                String css = "file:///" + fCustomStyle.getAbsolutePath().replace("\\", "/");
                log.info("Found custom stylesheet: " + css);
                this.scene.getStylesheets().clear();
                this.scene.getStylesheets().add(css);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public HydraScene() {}

    public void fireSceneSetEvent()
    {
        if (this.sceneSetEventHandler != null)
            sceneSetEventHandler.callback();
    }

    public void fireSceneShownEvent()
    {
        if (this.sceneShownEventHandler != null)
            sceneShownEventHandler.callback();
    }

    public Node getRoot()
    {
        return root;
    }

    public Scene getScene()
    {
        return scene;
    }

    public T getController()
    {
        return controller;
    }

    public HydraScene<T> duplicate()
    {
        HydraScene<T> newScene = new HydraScene<>();
        newScene.root = root;
        newScene.controller = controller;
        newScene.scene = scene;
        return newScene;
    }
}
