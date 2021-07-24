package org.gethydra.redux.backend.profiles;

import org.gethydra.redux.backend.LauncherVisibility;
import org.gethydra.redux.backend.mods.Mod;
import org.gethydra.redux.backend.mods.ModManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class LauncherProfile
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private String INTERNAL_UUID;
    private String name;
    private String gameDirectory;
    private int width, height;
    private boolean autoCrashReport;
    private LauncherVisibility launcherVisibility;
    private boolean snapshots, betas, alphas;
    private String selectedVersion;
    private String executable, arguments;
    private ArrayList<Mod> mods;
    private transient ModManager modManager;

    public void initModManager()
    {
        if (mods != null && modManager == null)
        {
            log.warning(String.format("Initializing mod manager for profile: '%s', with mods: %s", name, Arrays.toString(mods.toArray())));
            modManager = new ModManager(mods);
        }
    }

    public String getInternalUUID()
    {
        return INTERNAL_UUID;
    }

    public String getName()
    {
        return name;
    }

    public String getGameDirectory()
    {
        return gameDirectory;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean getAutoCrashReport()
    {
        return autoCrashReport;
    }

    public LauncherVisibility getLauncherVisibility()
    {
        return launcherVisibility;
    }

    public boolean areSnapshotsEnabled()
    {
        return snapshots;
    }

    public boolean areBetasEnabled()
    {
        return betas;
    }

    public boolean areAlphasEnabled()
    {
        return alphas;
    }

    public String getSelectedVersion()
    {
        return selectedVersion;
    }

    public String getExecutable()
    {
        return executable;
    }

    public String getArguments()
    {
        return arguments;
    }

    public ArrayList<Mod> getMods()
    {
        return mods;
    }

    public ModManager getModManager()
    {
        if (modManager == null)
            initModManager();
        return modManager;
    }

    public void setInternalUUID(String uuid)
    {
        INTERNAL_UUID = uuid;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setGameDirectory(String gameDirectory)
    {
        this.gameDirectory = gameDirectory;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public void setAutoCrashReport(boolean autoCrashReport)
    {
        this.autoCrashReport = autoCrashReport;
    }

    public void setLauncherVisibility(LauncherVisibility launcherVisibility)
    {
        this.launcherVisibility = launcherVisibility;
    }

    public void setSnapshotsEnabled(boolean snapshots)
    {
        this.snapshots = snapshots;
    }

    public void setBetasEnabled(boolean betas)
    {
        this.betas = betas;
    }

    public void setAlphasEnabled(boolean alphas)
    {
        this.alphas = alphas;
    }

    public void setSelectedVersion(String selectedVersion)
    {
        this.selectedVersion = selectedVersion;
    }

    public void setExecutable(String executable)
    {
        this.executable = executable;
    }

    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }

    public void setMods(ArrayList<Mod> mods)
    {
        this.mods = mods;
    }

    public String toString()
    {
        return getName();
    }
}
