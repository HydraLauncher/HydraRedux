package org.gethydra.redux.backend.versions;

import java.util.ArrayList;

public class VersionManifest
{
    public LatestVersion latest;
    public ArrayList<Version> versions;

    public VersionManifest() {}

    public VersionManifest(boolean test)
    {
        this.latest = new LatestVersion("b1.7.3", "b1.7.3");
        this.versions = new ArrayList<>();
        this.versions.add(new Version(true));
    }

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

        public LatestVersion() {}

        public LatestVersion(String release, String snapshot)
        {
            this.release = release;
            this.snapshot = snapshot;
        }
    }
}
