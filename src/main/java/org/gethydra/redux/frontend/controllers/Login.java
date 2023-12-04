package org.gethydra.redux.frontend.controllers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.DataStore;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.auth.Authenticator;
import org.gethydra.redux.backend.auth.LastLoginInfo;

import java.io.*;
import java.util.Objects;
import java.util.logging.Logger;

public class Login extends HydraController
{
    private static final Logger log = Logger.getLogger("HydraRedux");
    private static final Gson gson = new Gson();

    @FXML public Button btnLogin, btnCloseWindow, btnCreateAccount;

    @FXML public TextField txtUsername;
    @FXML public PasswordField txtPassword;

    @FXML public ImageView logo;

    @FXML public Label lblSignIn, lblEmail, lblPassword, lblLogin;

    @FXML public CheckBox cbRememberMe;

    @FXML
    protected void initialize()
    {
        File lastLoginFile = new File(Util.getHydraDirectory(), "lastlogin.json");

        setupButtonAnimation(btnLogin, 1.05D);
        setupButtonAnimation(btnCloseWindow);
        setupButtonAnimation(btnCreateAccount, 1.06D);
        setupComponentAnimation(cbRememberMe, 1.05D);

        btnLogin.setOnAction(e ->
        {
            try
            {
                Authenticator auth = new Authenticator(txtUsername.getText(), txtPassword.getText());
                AuthenticatedUser user = auth.authenticate(HydraRedux.getInstance().getMethodManager().getMethod("Hydra"));
                if (user != null) setLoggedIn(user.getUsername(), user.getSessionID());

                if (user != null && cbRememberMe.isSelected())
                {
                    LastLoginInfo lastLoginInfo = new LastLoginInfo(Objects.requireNonNull(user).getUsername(), user.getSessionID(), user.getClientToken());
                    try (JsonWriter writer = new JsonWriter(new FileWriter(lastLoginFile)))
                    {
                        gson.toJson(lastLoginInfo, LastLoginInfo.class, writer);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        });

        btnCreateAccount.setOnAction(e -> Util.openNetpage("https://os-mc.net/register"));

        btnCloseWindow.setOnAction(e -> HydraRedux.close());
    }

    private void setLoggedIn(String username, String accessToken)
    {
        log.info(String.format("Successfully logged in as: %s, access token: %s, profile: %s", username, System.getenv("HYDRA_DEBUG") != null ? accessToken : "<REDACTED>", HydraRedux.getInstance().getProfileManager().getSelectedProfile().getName()));

        DataStore store = HydraRedux.getInstance().getDataStore();
        store.setString("username", username);
        store.setString("accessToken", accessToken);

        SceneManager sceneManager = HydraRedux.getInstance().getSceneManager();
        if (sceneManager.getScene("Main") == null)
            log.severe("(Login) scene 'Main' == null");
        sceneManager.setScene(sceneManager.getScene("Main"));
    }

    @FXML protected void btnCloseHandle()
    {
        HydraRedux.close();
    }
}
