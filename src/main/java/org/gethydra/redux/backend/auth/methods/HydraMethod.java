package org.gethydra.redux.backend.auth.methods;

import javafx.scene.control.Alert;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.auth.AuthenticationMethod;
import org.gethydra.redux.backend.yggdrasil.YggdrasilAuthenticationResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HydraMethod extends AuthenticationMethod
{
    public HydraMethod()
    {
        super("Hydra");
    }

    public AuthenticatedUser authenticate(String identifier, String password)
    {
        try
        {
            String postEndpoint = "https://authserver.gethydra.org/authenticate";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(postEndpoint);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            StringEntity stringEntity = new StringEntity(gson.toJson(new YggdrasilRequest(identifier, password)));
            httpPost.setEntity(stringEntity);

            HttpResponse response = httpclient.execute(httpPost);

            HttpEntity responseEntity = response.getEntity();
            InputStream is = responseEntity.getContent();
            String contentEncoding = "UTF-8";
            String responseContent = IOUtils.toString(is, contentEncoding);
            JSONObject res = new JSONObject(responseContent);
            if (res.has("errorMessage"))
            {
                Util.alert("Oh noes!", res.getString("errorMessage"), Alert.AlertType.ERROR);
                return null;
            }
            if (!res.has("selectedProfile"))
            {
                Util.alert("Oh noes!", "Invalid username or password", Alert.AlertType.ERROR);
                return null;
            }
            YggdrasilAuthenticationResponse authResponse = gson.fromJson(responseContent, YggdrasilAuthenticationResponse.class);
            return new AuthenticatedUser(authResponse.selectedProfile.name, authResponse.accessToken, authResponse.clientToken);
        } catch (CancellationException | IOException ex) {
            //TODO: display error in a user-friendly way (and allow users to decide whether or not to report the error)
            ex.printStackTrace();
            return null;
        }
    }
}
