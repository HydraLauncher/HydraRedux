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
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
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
                showSelectedProfile(HydraRedux.getInstance().getProfileManager().getSelectedProfile());

        HydraRedux.getInstance().getProfileManager().getEventBus().subscribe((event) -> showSelectedProfile(HydraRedux.getInstance().getProfileManager().getSelectedProfile()));
    }

    private void showSelectedProfile(LauncherProfile profile)
    {
        BJManifest versionManifest = HydraRedux.getInstance().getVersionManifest();
        log.info("Showing profile editor: " + profile.getName());
        getController().txtProfileName.setText(profile.getName());

        getController().cbGameDirectory.setSelected(!profile.getGameDirectory().equals(Objects.requireNonNull(Util.getHydraDirectory()).getAbsolutePath()));
        getController().txtGameDirectory.setText(profile.getGameDirectory());
        getController().txtGameDirectory.setDisable(!getController().cbGameDirectory.isSelected());

        getController().cbResolution.setSelected(profile.getWidth() != 854 || profile.getHeight() != 480);

        getController().sWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999999));
        getController().sHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999999));
        getController().sWidth.getValueFactory().setValue(profile.getWidth());
        getController().sHeight.getValueFactory().setValue(profile.getHeight());
        getController().sWidth.setDisable(!getController().cbResolution.isSelected());

        getController().cbCrashReports.setSelected(profile.getAutoCrashReport());

        getController().cbLauncherVisibility.setSelected(profile.getLauncherVisibility() != LauncherVisibility.CLOSE);
        getController().cmbLauncherVisibility.getSelectionModel().select(profile.getLauncherVisibility());
        getController().cmbLauncherVisibility.setDisable(!getController().cbLauncherVisibility.isSelected());

        getController().cbSnapshots.setSelected(profile.areSnapshotsEnabled());
        getController().cbBetas.setSelected(profile.areBetasEnabled());
        getController().cbAlphas.setSelected(profile.areAlphasEnabled());

        ArrayList<BJManifest.BJVersionEntry> filteredVersions = new VersionFilter().filter(versionManifest.versions, profile.areSnapshotsEnabled(), profile.areBetasEnabled(), profile.areAlphasEnabled());
        getController().cmbVersion.setItems(FXCollections.observableArrayList(filteredVersions));
        getController().cmbVersion.getSelectionModel().select(versionManifest.find(profile.getSelectedVersion()));

        BJManifest.BJVersionEntry selectedVersion = HydraRedux.getInstance().getVersionManifest().find(profile.getSelectedVersion());
        if (selectedVersion == null) throw new RuntimeException("selectedVersion == null");
        JavaManager.JavaInstallation java = Objects.requireNonNull(JavaManager.JavaVersion.find(String.valueOf(selectedVersion.fetch().javaVersion.majorVersion))).constructInstallation();

        getController().cbExecutable.setSelected(!profile.getExecutable().equals(java.getJavaExecutable().getAbsolutePath()));
        getController().txtExecutable.setText(profile.getExecutable());
        getController().txtExecutable.setDisable(!getController().cbExecutable.isSelected());

        getController().cbArguments.setSelected(!profile.getArguments().equals("-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M"));
        getController().txtArguments.setText(profile.getArguments());
        getController().txtArguments.setDisable(!getController().cbArguments.isSelected());
    }
}
