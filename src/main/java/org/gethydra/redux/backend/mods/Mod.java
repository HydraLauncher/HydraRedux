package org.gethydra.redux.backend.mods;

import java.io.File;
import java.util.logging.Logger;

public class Mod
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private String name;
    private String path;
    private boolean enabled;

    public Mod() {}

    public Mod(String name, String path)
    {
        this.name = name;
        this.path = path;
        this.enabled = true;
    }

    public String getName()
    {
        return name;
    }

    public File getFile()
    {
        return new File(path);
    }

    public String toString()
    {
        return getName();
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean flag)
    {
        enabled = flag;
        log.info(String.format("Mod '%s' is now %s", getName(), flag ? "enabled" : "disabled"));
    }
}
