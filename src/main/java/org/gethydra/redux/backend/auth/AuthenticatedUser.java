package org.gethydra.redux.backend.auth;

public class AuthenticatedUser
{
    private String username;
    private String sessionID;
    private String clientToken;

    public AuthenticatedUser(String username, String sessionID, String clientToken)
    {
        this.username = username;
        this.sessionID = sessionID;
        this.clientToken = clientToken;
    }

    public String getUsername()
    {
        return username;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public String getClientToken()
    {
        return clientToken;
    }
}
