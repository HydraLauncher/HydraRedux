package org.gethydra.redux.frontend.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;

public class HydraController
{
    private static final boolean ANIMATION_DISABLED = true;

    @FXML public Pane background;

    protected void setupButtonAnimation(Button button)
    {
        setupButtonAnimation(button,1.1D);
    }

    protected void setupButtonAnimation(Button button, double toScale)
    {
        if (ANIMATION_DISABLED) return;
        button.setOnMouseEntered((e) -> scaleTransition(button, toScale));
        button.setOnMouseExited((e) -> scaleTransition(button, 1.0D));
    }

    protected void setupComponentAnimation(Node node, double toScale)
    {
        if (ANIMATION_DISABLED) return;
        node.setOnMouseEntered((e) -> scaleTransition(node, toScale));
        node.setOnMouseExited((e) -> scaleTransition(node, 1.0D));
    }

    protected void scaleTransition(Node node, double toScale)
    {
        scaleTransition(node, toScale, 200);
    }

    protected void scaleTransition(Node node, double toScale, double durationMillis)
    {
        if (ANIMATION_DISABLED) return;
        Duration duration = Duration.millis(durationMillis);
        ScaleTransition scaleTransition = new ScaleTransition(duration, node);
        scaleTransition.setToX(toScale);
        scaleTransition.setToY(toScale);
        scaleTransition.play();
    }

    protected void fadeTransition(Node node, double toOpacity)
    {
        fadeTransition(node, toOpacity, 200);
    }

    protected void fadeTransition(Node node, double toOpacity, double durationMillis)
    {
        if (ANIMATION_DISABLED) return;
        Duration duration = Duration.millis(durationMillis);
        FadeTransition transition = new FadeTransition(duration, node);
        transition.setToValue(toOpacity);
        transition.play();
    }
}
