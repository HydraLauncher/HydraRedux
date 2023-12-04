package org.gethydra.redux.frontend;

import javafx.scene.control.Alert;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.backend.download.DownloadTracker;
import org.gethydra.redux.frontend.controllers.Main;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.download.EventHandler;
import org.gethydra.redux.backend.download.HydraEvent;
import org.gethydra.redux.backend.profiles.LauncherProfile;
import org.gethydra.redux.frontend.controllers.HydraScene;

import java.util.logging.Logger;

public class FrontendDownloadTracker implements EventHandler
{
    private static final Logger log = Logger.getLogger("HydraRedux");
    private DownloadTracker tracker;

    public FrontendDownloadTracker(DownloadTracker tracker)
    {
        this.tracker = tracker;
    }

    @Override
    public void fire(HydraEvent status)
    {
        LauncherProfile selectedProfile = HydraRedux.getInstance().getProfileManager().getSelectedProfile();
        HydraScene<Main> main = HydraRedux.getInstance().getSceneManager().getScene("Main");
        Main ctrl = main.getController();
        switch (status)
        {
            case DOWNLOAD_STARTED:
                ctrl.pBar.setVisible(true);
                break;
            case DOWNLOAD_TRACKER_UPDATE:
                ctrl.pBar.progressProperty().set((double)tracker.getDownloadedBytes() / (double)tracker.getTotalBytes());
                ctrl.pBar.getStyleClass().remove(".dl-completed");
                ctrl.pBar.getStyleClass().remove(".dl-failed");
                break;
            case DOWNLOAD_FAILED:
                log.severe("Download failed");
                ctrl.pBar.setProgress(0.0D);
                ctrl.pBar.setVisible(false);
                ctrl.setLocked(false);
                break;
            case GAME_STARTED:
                log.warning("Game started");
                ctrl.pBar.setVisible(false);
                switch (selectedProfile.getLauncherVisibility())
                {
                    default:
                        break;
                    case CLOSE:
                        System.exit(0);
                        break;
                    case KEEP_OPEN:
                        ctrl.setLocked(true);
                        break;
                    case HIDE_THEN_OPEN:
                        HydraRedux.getInstance().getSceneManager().hide();
                        break;
                }
                //setTab(HydraRedux.getInstance().getSceneManager().<News>getScene("News").getController().background);
                break;
            case GAME_CLOSED:
                log.warning("Game closed with exit code: " + tracker.getExitCode());
                if (tracker.getExitCode() != 0) Util.alert("Oh noes!", "The game crashed!", Alert.AlertType.ERROR);
                switch (HydraRedux.getInstance().getProfileManager().getSelectedProfile().getLauncherVisibility())
                {
                    default:
                    case KEEP_OPEN:
                        ctrl.setLocked(false);
                        break;
                    case CLOSE:
                        ctrl.setLocked(false);
                        System.exit(0);
                        break;
                    case HIDE_THEN_OPEN:
                        HydraRedux.getInstance().getSceneManager().show();
                        ctrl.setLocked(false);
                        break;
                }
        }
    }
}
