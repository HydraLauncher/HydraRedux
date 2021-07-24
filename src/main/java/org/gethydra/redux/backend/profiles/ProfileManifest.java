package org.gethydra.redux.backend.profiles;

import java.util.ArrayList;

public class ProfileManifest
{
    public String selectedProfile;
    public ArrayList<LauncherProfile> profiles;

    public ProfileManifest()
    {
        selectedProfile = "";
        profiles = new ArrayList<>();
    }

    public ProfileManifest(LauncherProfile defaultProfile)
    {
        this.selectedProfile = defaultProfile.getName();
        this.profiles = new ArrayList<>();
        this.profiles.add(defaultProfile);
    }
}
