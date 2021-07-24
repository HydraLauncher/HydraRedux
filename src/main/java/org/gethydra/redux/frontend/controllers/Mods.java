package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.mods.Mod;
import org.gethydra.redux.backend.mods.ModManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Mods extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Button btnAddMod, btnRemoveMod, btnMoveUp, btnMoveDown, btnOpenHydra;
    @FXML public Label lblNoMods;
    @FXML public VBox modBox;

    @FXML protected void initialize()
    {
        HydraRedux.getInstance().getProfileManager().getSelectedProfile().getModManager().getEventBus().subscribe(() ->
        {
            log.warning("ModManager event bus fired");

            HydraRedux.getInstance().getProfileManager().saveToDisk();
            refreshModView();
        });

        setupButtonAnimation(btnAddMod);
        setupButtonAnimation(btnRemoveMod);
        setupButtonAnimation(btnMoveUp);
        setupButtonAnimation(btnMoveDown);
        setupButtonAnimation(btnOpenHydra);

        btnAddMod.setOnAction((e) ->
        {
            ModManager modManager = HydraRedux.getInstance().getProfileManager().getSelectedProfile().getModManager();

            FileChooser fc = new FileChooser();
            fc.setTitle("Select mod(s)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MOD files", "*.jar", "*.zip"));
            List<File> files = fc.showOpenMultipleDialog(HydraRedux.getInstance().getSceneManager().getPrimaryStage());
            if (files != null && files.size() > 0)
                files.forEach(file -> modManager.add(new Mod(file.getName(), file.getAbsolutePath())));
            refreshModView();
            modBox.requestFocus();
            refreshModView();
        });

        refreshModView();
    }

    public void clearMods()
    {
        if (modBox != null && modBox.getChildren() != null) modBox.getChildren().clear();
    }

    public void refreshModView()
    {
        clearMods();

        ModManager modManager = HydraRedux.getInstance().getProfileManager().getSelectedProfile().getModManager();
        modManager.getMods().forEach(this::addMod);
        lblNoMods.setVisible(modManager.getMods().size() < 1);
        log.warning("Refreshed mod view: " + modManager.getMods().toString());
    }

    private void addMod(Mod mod)
    {
        HydraScene<ModEntry> entry = new HydraScene<>("/assets/fxml/ModEntry.fxml");

        entry.getController().lblName.setText(mod.getName());
        entry.getController().lblDescription.setText(String.format("A .%s mod", FilenameUtils.getExtension(mod.getFile().getName())));
        entry.getController().cbEnabled.setSelected(mod.isEnabled());
        //TODO: mod description & icon

        modBox.getChildren().add(entry.getRoot());
    }
}
