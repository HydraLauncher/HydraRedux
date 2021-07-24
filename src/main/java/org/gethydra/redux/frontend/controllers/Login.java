package org.gethydra.redux.frontend.controllers;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.auth.Authenticator;

import java.util.logging.Logger;

public class Login extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    @FXML public Button btnLogin, btnCloseWindow, btnCreateAccount;

    @FXML public TextField txtUsername;
    @FXML public PasswordField txtPassword;

    @FXML public Pane logo, betaWarning;

    @FXML public Label lblSignIn, lblEmail, lblPassword, lblLogin;

    @FXML public CheckBox cbRememberMe;

    @FXML
    protected void initialize()
    {
        setupButtonAnimation(btnLogin, 1.05D);
        setupButtonAnimation(btnCloseWindow);
        setupButtonAnimation(btnCreateAccount, 1.06D);

        btnLogin.setOnAction(e ->
        {
            try
            {
                Authenticator auth = new Authenticator(txtUsername.getText(), txtPassword.getText());
                AuthenticatedUser user = auth.authenticate(HydraRedux.getInstance().getMethodManager().getMethod("Hydra"));
                if (user != null)
                {
                    log.info(String.format("Successfully logged in as: %s, access token: %s, profile: %s", user.getUsername(), System.getenv("HYDRA_DEBUG") != null ? user.getSessionID() : "<REDACTED>", HydraRedux.getInstance().getProfileManager().getSelectedProfile().getName()));

                    DataStore store = HydraRedux.getInstance().getDataStore();
                    store.setString("username", user.getUsername());
                    store.setString("accessToken", user.getSessionID());

                    SceneManager sceneManager = HydraRedux.getInstance().getSceneManager();
                    if (sceneManager.getScene("Main") == null)
                        log.severe("(Login) scene 'Main' == null");
                    sceneManager.setScene(sceneManager.getScene("Main"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnCreateAccount.setOnAction(e ->
        {
            Util.openNetpage("https://beta.oldschoolminecraft.net/register");
        });

        btnCloseWindow.setOnAction(e -> HydraRedux.close());
    }

    @FXML protected void btnCloseHandle()
    {
        HydraRedux.close();
    }
}
