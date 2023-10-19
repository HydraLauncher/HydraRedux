package org.gethydra.redux.backend.auth;

public class LastLoginInfo
{
    public String accessToken;
    public String clientToken;
    public String username;

    public LastLoginInfo() {}

    public LastLoginInfo(String username, String accessToken, String clientToken)
    {
        this.username = username;
        this.accessToken = accessToken;
        this.clientToken = clientToken;
    }
}
