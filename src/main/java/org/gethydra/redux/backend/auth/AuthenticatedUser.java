package org.gethydra.redux.backend.auth;

public class AuthenticatedUser
{
    private String username;
    private String sessionID;

    public AuthenticatedUser(String username, String sessionID)
    {
        this.username = username;
        this.sessionID = sessionID;
    }

    public String getUsername()
    {
        return username;
    }

    public String getSessionID()
    {
        return sessionID;
    }
}
