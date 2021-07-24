package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Error extends HydraController
{
    @FXML public Button btnReport, btnIgnore;

    @FXML protected void initialize()
    {
        setupButtonAnimation(btnReport);
        setupButtonAnimation(btnIgnore);
    }
}
