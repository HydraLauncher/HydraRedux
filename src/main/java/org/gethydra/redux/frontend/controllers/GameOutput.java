package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import org.gethydra.redux.backend.GameOutputStream;

import java.io.PrintStream;

public class GameOutput extends HydraController
{
    @FXML public TextArea outputField;

    @FXML protected void initialize()
    {
        //System.setErr(new PrintStream(new GameOutputStream(outputField)));
        //System.setOut(new PrintStream(new GameOutputStream(outputField)));
    }
}
