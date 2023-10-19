package org.gethydra.redux.backend.auth;

public class SessionValidateRequest
{
    public String accessToken;
    public String clientToken;

    public SessionValidateRequest() {}

    public SessionValidateRequest(String accessToken, String clientToken)
    {
        this.accessToken = accessToken;
        this.clientToken = clientToken;
    }
}
