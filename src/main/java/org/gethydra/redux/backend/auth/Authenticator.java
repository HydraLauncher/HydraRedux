package org.gethydra.redux.backend.auth;

public class Authenticator
{
    private String identifier;
    private String password;

    public Authenticator(String identifier, String password)
    {
        this.identifier = identifier;
        this.password = password;
    }

    public AuthenticatedUser authenticate(AuthenticationMethod method)
    {
        return method.authenticate(identifier, password);
    }
}
