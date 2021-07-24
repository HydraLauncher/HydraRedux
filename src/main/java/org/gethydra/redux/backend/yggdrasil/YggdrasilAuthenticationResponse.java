package org.gethydra.redux.backend.yggdrasil;

public class YggdrasilAuthenticationResponse
{
    public YggdrasilUser user;
    public String clientToken;
    public String accessToken;
    public YggdrasilProfile[] availableProfiles;
    public YggdrasilProfile selectedProfile;
}
