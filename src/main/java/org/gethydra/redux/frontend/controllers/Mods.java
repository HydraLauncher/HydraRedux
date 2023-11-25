package org.gethydra.redux.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.mods.Mod;
import org.gethydra.redux.backend.mods.ModManager;
import org.gethydra.redux.backend.profiles.LauncherProfile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Mods extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Button btnAddMod, btnOpenHydra;
    @FXML public Label lblNoMods;
    @FXML public VBox modBox;

    @FXML protected void initialize()
    {
        HydraRedux.getInstance().getProfileManager().getEventBus().subscribe((event) ->
        {
            log.warning("ModManager event bus fired");

            HydraRedux.getInstance().getProfileManager().saveToDisk();
            refreshModView();
        });

        setupButtonAnimation(btnAddMod);
        setupButtonAnimation(btnOpenHydra);

        btnAddMod.setOnAction((e) ->
        {
            LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
            ModManager modManager = selectedProfile.getModManager();

            File modsDir = new File(selectedProfile.getGameDirectory(), "mods/");
            modsDir.mkdirs();

            FileChooser fc = new FileChooser();
            fc.setTitle("Select mod(s)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("MOD files", "*.jar", "*.zip"));
            List<File> files = fc.showOpenMultipleDialog(HydraRedux.getInstance().getSceneManager().getPrimaryStage());
            if (files != null && !files.isEmpty())
                files.forEach(file ->
                {
                    try
                    {
                        File newModFile = new File(modsDir, file.getName());
                        Files.copy(Paths.get(file.getAbsolutePath()), Paths.get(newModFile.getAbsolutePath()));
                        modManager.add(new Mod(file.getName(), newModFile.getAbsolutePath()));
                    } catch (Exception ex) {
                        Util.alert("Oh noes!", "Failed to add mod:" + System.lineSeparator() + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                });
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
