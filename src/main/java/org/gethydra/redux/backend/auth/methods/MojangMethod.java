package org.gethydra.redux.backend.auth.methods;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.gethydra.redux.backend.auth.AuthenticatedUser;
import org.gethydra.redux.backend.auth.AuthenticationMethod;
import org.gethydra.redux.backend.yggdrasil.YggdrasilAuthenticationResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MojangMethod extends AuthenticationMethod
{
    public MojangMethod()
    {
        super("Mojang");
    }

    public AuthenticatedUser authenticate(String identifier, String password)
    {
        try
        {
            HttpAsyncClient client = HttpAsyncClients.createDefault();
            HttpPost request = new HttpPost("https://authserver.mojang.com/authenticate");
            Future<HttpResponse> future = client.execute(request, null);
            HttpResponse response = future.get();
            if (response.getStatusLine().getStatusCode() == 200)
            {
                YggdrasilAuthenticationResponse authResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent(), response.getEntity().getContentEncoding().getName()), YggdrasilAuthenticationResponse.class);
                return new AuthenticatedUser(authResponse.selectedProfile.id, authResponse.accessToken);
            }
            return null;
        } catch (InterruptedException | ExecutionException | CancellationException | IOException ex) {
            //TODO: display errors in a user-friendly way (and allow users to decide whether or not to report the error)
            ex.printStackTrace();
            return null;
        }
    }
}
