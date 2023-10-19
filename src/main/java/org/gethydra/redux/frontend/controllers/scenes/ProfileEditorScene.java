package org.gethydra.redux.frontend.controllers.scenes;

import javafx.collections.FXCollections;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.JavaManager;
import org.gethydra.redux.backend.LauncherVisibility;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.VersionFilter;
import org.gethydra.redux.backend.versions.VersionManifest;
import org.gethydra.redux.frontend.controllers.HydraScene;
import org.gethydra.redux.frontend.controllers.ProfileEditor;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public class ProfileEditorScene extends HydraScene<ProfileEditor>
{
    private static Logger log = Logger.getLogger("HydraRedux");

    public ProfileEditorScene()
    {
        super("/assets/fxml/ProfileEditor.fxml");

        this.sceneShownEventHandler = () ->
        {
            VersionManifest versionManifest = HydraRedux.getInstance().getVersionManifest();
            LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
            log.info("Showing profile editor: " + selectedProfile.getName());
            getController().txtProfileName.setText(selectedProfile.getName());

            getController().cbGameDirectory.setSelected(!selectedProfile.getGameDirectory().equals(Objects.requireNonNull(Util.getHydraDirectory()).getAbsolutePath()));
            getController().txtGameDirectory.setText(selectedProfile.getGameDirectory());
            getController().txtGameDirectory.setDisable(!getController().cbGameDirectory.isSelected());

            getController().cbResolution.setSelected(selectedProfile.getWidth() != 854 || selectedProfile.getHeight() != 480);

            getController().sWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999999));
            getController().sHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999999));
            getController().sWidth.getValueFactory().setValue(selectedProfile.getWidth());
            getController().sHeight.getValueFactory().setValue(selectedProfile.getHeight());
            getController().sWidth.setDisable(!getController().cbResolution.isSelected());

            getController().cbCrashReports.setSelected(selectedProfile.getAutoCrashReport());

            getController().cbLauncherVisibility.setSelected(selectedProfile.getLauncherVisibility() != LauncherVisibility.CLOSE);
            getController().cmbLauncherVisibility.getSelectionModel().select(selectedProfile.getLauncherVisibility());
            getController().cmbLauncherVisibility.setDisable(!getController().cbLauncherVisibility.isSelected());

            getController().cbSnapshots.setSelected(selectedProfile.areSnapshotsEnabled());
            getController().cbBetas.setSelected(selectedProfile.areBetasEnabled());
            getController().cbAlphas.setSelected(selectedProfile.areAlphasEnabled());

            ArrayList<Version> filteredVersions = new VersionFilter().filter(versionManifest.versions, selectedProfile.areSnapshotsEnabled(), selectedProfile.areBetasEnabled(), selectedProfile.areAlphasEnabled());
            getController().cmbVersion.setItems(FXCollections.observableArrayList(filteredVersions));
            getController().cmbVersion.getSelectionModel().select(versionManifest.find(selectedProfile.getSelectedVersion()));

            Version selectedVersion = HydraRedux.getInstance().getVersionManifest().find(selectedProfile.getSelectedVersion());
            assert selectedVersion != null;
            JavaManager.JavaInstallation java = Objects.requireNonNull(JavaManager.JavaVersion.find(selectedVersion.java_version)).constructInstallation();

            getController().cbExecutable.setSelected(!selectedProfile.getExecutable().equals(java.getJavaExecutable().getAbsolutePath()));
            getController().txtExecutable.setText(selectedProfile.getExecutable());
            getController().txtExecutable.setDisable(!getController().cbExecutable.isSelected());

            getController().cbArguments.setSelected(!selectedProfile.getArguments().equals("-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M"));
            getController().txtArguments.setText(selectedProfile.getArguments());
            getController().txtArguments.setDisable(!getController().cbArguments.isSelected());
        };
    }
}
