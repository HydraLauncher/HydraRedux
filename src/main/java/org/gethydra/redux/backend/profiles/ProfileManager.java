package org.gethydra.redux.backend.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.EventPipe;
import org.gethydra.redux.backend.JavaManager;
import org.gethydra.redux.backend.LauncherVisibility;
import org.gethydra.redux.backend.VoidPipe;
import org.gethydra.redux.backend.versions.Version;
import org.gethydra.redux.backend.versions.betterjsons.BJManifest;
import org.gethydra.redux.event.EventBus;
import org.gethydra.redux.event.Events;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class ProfileManager
{
    private static final Logger log = Logger.getLogger("HydraRedux");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final EventBus<EventPipe> eventBus = new EventBus<>();

    private ProfileManifest profileManifest;
    private final File fileLauncherProfiles;

    public ProfileManager()
    {
        this.fileLauncherProfiles = new File(Util.getHydraDirectory(), "launcher_profiles.json");

        sanity();
    }

    private void sanity()
    {
        try
        {
            if (fileLauncherProfiles.createNewFile())
                log.warning("Launcher profiles missing. A new file has been generated.");
            else
                log.info("Launcher profiles file exists. No action taken.");
        } catch (Exception ex) {
            log.severe("Failed to check sanity of launcher profiles");
        }
    }

    public EventBus<EventPipe> getEventBus()
    {
        return eventBus;
    }

    public ProfileManifest getProfileManifest()
    {
        return profileManifest;
    }

    public ArrayList<LauncherProfile> getProfiles()
    {
        return profileManifest.profiles;
    }

    public LauncherProfile getSelectedProfile()
    {
        LauncherProfile profile = getProfile(profileManifest.selectedProfile);
        if (profile != null)
        {
            return profile;
        } else {
            log.warning(String.format("Selected profile '%s' doesn't exist. Returning default profile instead.", profileManifest.selectedProfile));
            profile = getDefaultProfile();
            profileManifest.selectedProfile = profile.getName();
            return profile;
        }
    }

    public void fireEventBus(Events event)
    {
        for (EventPipe handler : eventBus.getSubscribers())
            handler.flush(event);
    }

    public void setSelectedProfile(LauncherProfile profile)
    {
        setSelectedProfile(profile, true);
    }

    public void setSelectedProfile(LauncherProfile profile, boolean fireEventBus)
    {
        if (profile == null)
        {
            log.severe("Failed to set selected profile: profile == null");
            Thread.dumpStack();
            System.exit(-1);
            return;
        }
        profileManifest.selectedProfile = profile.getName();
        saveToDisk();
        if (fireEventBus) fireEventBus(Events.PROFILE_SELECTED);
    }

    public LauncherProfile getProfile(String name)
    {
        for (LauncherProfile profile : profileManifest.profiles)
            if (profile.getName().equalsIgnoreCase(name))
                return profile;
        return null;
    }

    public void addAndSave(LauncherProfile profile) throws IOException
    {
        profileManifest.profiles.add(profile);
        saveToDisk();
        fireEventBus(Events.PROFILE_ADDED);
    }

    public void removeAndSave(String name) throws IOException
    {
        profileManifest.profiles.removeIf(profile -> profile.getName().equals(name));
        saveToDisk();
        fireEventBus(Events.PROFILE_REMOVED);
    }

    public void updateAndSave(LauncherProfile profile) throws IOException
    {
        profileManifest.profiles.removeIf(p -> p.getInternalUUID().equals(profile.getInternalUUID()));
        profileManifest.profiles.add(profile);
        saveToDisk();
        fireEventBus(Events.PROFILE_UPDATED);
    }

    public void loadFromDisk()
    {
        try
        {
            try (Reader reader = new FileReader(fileLauncherProfiles))
            {
                profileManifest = gson.fromJson(new JsonReader(new FileReader(fileLauncherProfiles)), ProfileManifest.class);
                log.warning("Loaded profiles from disk");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void saveToDisk()
    {
        try
        {
            try (Writer writer = new FileWriter(fileLauncherProfiles))
            {
                if (profileManifest == null)
                    profileManifest = new ProfileManifest(getDefaultProfile());
                profileManifest.profiles.forEach(profile -> profile.setMods(profile.getModManager().getMods()));
                gson.toJson(profileManifest, writer);
                log.warning("Saved profiles to disk");
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private LauncherProfile getDefaultProfile()
    {
        return createNewProfile("Default");
    }

    public LauncherProfile createNewProfile(String name)
    {
        BJManifest.BJVersionEntry version = HydraRedux.getInstance().getVersionManifest().find("b1.7.3"); // b1.7.3 is always default
        LauncherProfile profile = new LauncherProfile();
        profile.setInternalUUID(UUID.randomUUID().toString());
        profile.setName(name);
        profile.setGameDirectory(new File(Util.getHydraDirectory(), "profiles/" + name).getAbsolutePath());
        profile.setWidth(854);
        profile.setHeight(480);
        profile.setAutoCrashReport(true);
        profile.setSnapshotsEnabled(false);
        profile.setBetasEnabled(true);
        profile.setAlphasEnabled(true);
        profile.setSelectedVersion(version.id);
        profile.setLauncherVisibility(LauncherVisibility.CLOSE);
        profile.setExecutable(Objects.requireNonNull(JavaManager.JavaVersion.find("8")).constructInstallation().getJavaExecutable().getAbsolutePath());
        profile.setArguments("-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M");
        profile.setMods(new ArrayList<>());
        return profile;
    }
}
