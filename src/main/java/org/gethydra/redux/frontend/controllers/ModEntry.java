package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.mods.ModManager;

public class ModEntry extends HydraController
{
    @FXML public Pane icon;
    @FXML public Label lblName, lblDescription;
    @FXML public CheckBox cbEnabled;
    @FXML public Button btnRemove, btnMoveUp, btnMoveDown;

    @FXML protected void initialize()
    {
        ModManager modManager = HydraRedux.getInstance().getProfileManager().getSelectedProfile().getModManager();

        setupButtonAnimation(btnRemove, 1.05D);
        setupButtonAnimation(btnMoveUp, 1.05D);
        setupButtonAnimation(btnMoveDown, 1.05D);

        cbEnabled.setOnAction((e) -> modManager.get(lblName.getText()).setEnabled(cbEnabled.isSelected()));
        btnRemove.setOnAction((e) -> modManager.remove(lblName.getText()));
        btnMoveUp.setOnAction((e) -> modManager.moveUp(lblName.getText()));
        btnMoveDown.setOnAction((e) -> modManager.moveDown(lblName.getText()));
    }
}
