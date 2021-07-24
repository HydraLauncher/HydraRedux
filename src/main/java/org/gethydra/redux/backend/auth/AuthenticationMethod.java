package org.gethydra.redux.backend.auth;

import com.google.gson.Gson;

public abstract class AuthenticationMethod
{
    protected static final Gson gson = new Gson();

    private String friendlyName;

    public AuthenticationMethod(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }

    public abstract AuthenticatedUser authenticate(String identifier, String password);

    public String getFriendlyName()
    {
        return friendlyName;
    }
}
