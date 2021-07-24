package org.gethydra.redux.backend.versions;

import java.util.ArrayList;

public class VersionManifest
{
    public LatestVersion latest;
    public ArrayList<Version> versions;

    public Version find(String id)
    {
        for (Version v : versions)
            if (v.id.equals(id))
                return v;
        return null;
    }

    public static class LatestVersion
    {
        public String release;
        public String snapshot;
    }
}
