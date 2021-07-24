package org.gethydra.redux.backend.versions;

import org.gethydra.redux.Util;

public class VersionNatives
{
    public Library windows, mac, linux;

    public VersionNatives() {}

    public VersionNatives(Library windows, Library mac, Library linux)
    {
        this.windows = windows;
        this.mac = mac;
        this.linux = linux;
    }

    public Library getForPlatform(Util.OS os)
    {
        switch (os)
        {
            case Windows:
                return windows;
            case MacOS:
                return mac;
            case Linux:
                return linux;
        }

        return linux;
    }
}
