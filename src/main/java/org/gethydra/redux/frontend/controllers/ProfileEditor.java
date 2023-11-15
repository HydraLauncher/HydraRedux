package org.gethydra.redux.frontend.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.LauncherVisibility;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.profiles.ProfileManager;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
import org.gethydra.redux.event.Events;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class ProfileEditor extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Button btnCancel, btnOpenGameDir, btnSaveProfile;
    @FXML public CheckBox cbGameDirectory, cbResolution, cbCrashReports, cbLauncherVisibility, cbSnapshots, cbBetas, cbAlphas, cbExecutable, cbArguments;
    @FXML public TextField txtProfileName, txtGameDirectory, txtExecutable, txtArguments;
    @FXML public Spinner<Integer> sWidth, sHeight;
    @FXML public ComboBox<LauncherVisibility> cmbLauncherVisibility;
    @FXML public ComboBox<BJManifest.BJVersionEntry> cmbVersion;
    @FXML public Label lblUnsavedChanges;

    @FXML protected void initialize()
    {
        HydraRedux.getInstance().getProfileManager().getEventBus().subscribe((event) ->
        {
            if (event == Events.PROFILE_UPDATED)
            {
                LauncherProfile profile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
                txtGameDirectory.setText(profile.getGameDirectory());
                txtProfileName.setText(profile.getName());
                txtExecutable.setText(profile.getExecutable());
                txtArguments.setText(profile.getArguments());
                sWidth.getValueFactory().setValue(profile.getWidth());
                sHeight.getValueFactory().setValue(profile.getHeight());
                cmbLauncherVisibility.getSelectionModel().select(profile.getLauncherVisibility());
                thinkAboutChange();
            }
        });

        thinkAboutChange();

        cbGameDirectory.setOnAction((e) ->
        {
            txtGameDirectory.setDisable(!cbGameDirectory.isSelected());
            thinkAboutChange();
        });

        cbResolution.setOnAction((e) ->
        {
            sWidth.setDisable(!cbResolution.isSelected());
            sHeight.setDisable(!cbResolution.isSelected());
            thinkAboutChange();
        });

        cbLauncherVisibility.setOnAction((e) ->
        {
            cmbLauncherVisibility.setDisable(!cbLauncherVisibility.isSelected());
            thinkAboutChange();
        });

        cbExecutable.setOnAction((e) ->
        {
            txtExecutable.setDisable(!cbExecutable.isSelected());
            thinkAboutChange();
        });

        cbArguments.setOnAction((e) ->
        {
            txtArguments.setDisable(!cbArguments.isSelected());
            thinkAboutChange();
        });

        setupButtonAnimation(btnCancel);
        setupButtonAnimation(btnOpenGameDir);
        setupButtonAnimation(btnSaveProfile);

        btnCancel.setOnAction((e) ->
        {
            // reload old values
            HydraRedux.getInstance().getSceneManager().<ProfileEditor>getScene("ProfileEditor").fireSceneShownEvent();
            thinkAboutChange();
        });

        btnOpenGameDir.setOnAction((e) ->
        {
            Util.openNetpage(Objects.requireNonNull(Util.getHydraDirectory()).getAbsolutePath());
            thinkAboutChange();
        });

        btnSaveProfile.setOnAction((e) ->
        {
            ProfileManager pm = HydraRedux.getInstance().getProfileManager();
            LauncherProfile selectedProfile = pm.getSelectedProfile();

            selectedProfile.setName(txtProfileName.getText());
            selectedProfile.setGameDirectory(txtGameDirectory.getText());
            selectedProfile.setWidth(sWidth.getValue());
            selectedProfile.setHeight(sHeight.getValue());
            selectedProfile.setLauncherVisibility(cmbLauncherVisibility.getValue());
            selectedProfile.setSnapshotsEnabled(cbSnapshots.isSelected());
            selectedProfile.setBetasEnabled(cbBetas.isSelected());
            selectedProfile.setAlphasEnabled(cbAlphas.isSelected());
            selectedProfile.setSelectedVersion(cmbVersion.getValue().id);
            selectedProfile.setExecutable(txtExecutable.getText());
            selectedProfile.setArguments(txtArguments.getText());

            try
            {
                pm.updateAndSave(selectedProfile);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }

            pm.setSelectedProfile(selectedProfile);
            thinkAboutChange();
        });

        refreshVersions();
        refreshVisibilityOptions();

        txtProfileName.textProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());
        txtGameDirectory.textProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());
        txtExecutable.textProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());
        txtArguments.textProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());

        sWidth.valueProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());
        sHeight.valueProperty().addListener((observable, oldValue, newValue) -> thinkAboutChange());

        cmbLauncherVisibility.setOnAction((e) -> thinkAboutChange()); //TODO: set profile visibility when this option is changed
        cmbVersion.setOnAction((e) -> thinkAboutChange());

        thinkAboutChange();
    }

    private void thinkAboutChange()
    {
        boolean somethingChanged = didSomethingChange();
        btnSaveProfile.setDisable(!somethingChanged);
        btnCancel.setDisable(!somethingChanged);
        if (somethingChanged) fadeTransition(lblUnsavedChanges, 1.0D);
        else fadeTransition(lblUnsavedChanges, 0.0D);
    }

    private void refreshVersions()
    {
        try
        {
            if (cmbVersion == null) return;
            if (HydraRedux.getInstance() == null) return;
            if (HydraRedux.getInstance().getVersionManifest() == null) return;
            if (HydraRedux.getInstance().getVersionManifest().versions == null) return;
            cmbVersion.getItems().clear();
            ArrayList<BJManifest.BJVersionEntry> versions = HydraRedux.getInstance().getVersionManifest().versions;
            cmbVersion.setItems(FXCollections.observableArrayList(versions));
        } catch (Exception ex) {
            log.severe("Something went wrong while refreshing the versions list in the Profile Editor: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private void refreshVisibilityOptions()
    {
        cmbLauncherVisibility.getItems().clear();
        cmbLauncherVisibility.setItems(FXCollections.observableArrayList(LauncherVisibility.values()));
    }

    private boolean didSomethingChange()
    {
        LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();

        if (selectedProfile == null)
        {
            log.severe("Failed to check if profile editor data changed: selectedProfile == null");
            return false;
        }

        if (!txtProfileName.getText().equals(selectedProfile.getName()))
            return true;
        if (!txtGameDirectory.getText().equals(selectedProfile.getGameDirectory()))
            return true;
        if (sWidth.valueProperty().getValue() != null && sWidth.valueProperty().getValue() != selectedProfile.getWidth())
            return true;
        if (sHeight.valueProperty().getValue() != null && sHeight.valueProperty().getValue() != selectedProfile.getHeight())
            return true;
        if (cbCrashReports.isSelected() != selectedProfile.getAutoCrashReport())
            return true;
        if (cmbLauncherVisibility.valueProperty().getValue() != null && cmbLauncherVisibility.valueProperty().getValue() != selectedProfile.getLauncherVisibility())
            return true;
        if (cbSnapshots.isSelected() != selectedProfile.areSnapshotsEnabled())
            return true;
        if (cbBetas.isSelected() != selectedProfile.areBetasEnabled())
            return true;
        if (cbAlphas.isSelected() != selectedProfile.areAlphasEnabled())
            return true;
        if (cmbVersion.valueProperty().getValue() != null && !cmbVersion.valueProperty().getValue().id.equals(selectedProfile.getSelectedVersion()))
            return true;
        if (!txtArguments.getText().equals(selectedProfile.getArguments()))
            return true;

        return false;
    }
}
