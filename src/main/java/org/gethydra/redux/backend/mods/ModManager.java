package org.gethydra.redux.backend.mods;

import org.gethydra.redux.backend.VoidPipe;
import org.gethydra.redux.event.EventBus;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ModManager
{
    private static final Logger log = Logger.getLogger("HydraRedux");

    private final EventBus<VoidPipe> eventBus = new EventBus<>();

    private ArrayList<Mod> mods;

    public ModManager(ArrayList<Mod> mods)
    {
        this.mods = mods;
    }

    public void add(Mod mod)
    {
        mods.add(mod);
        log.warning("Added mod: " + mod.getName());
        eventBus.getSubscribers().forEach(pipe -> pipe.callback());
    }

    public void remove(String name)
    {
        mods.removeIf(mod -> mod.getName().equals(name));
        log.warning("Removed mod: " + name);
        eventBus.getSubscribers().forEach(VoidPipe::callback);
    }

    public void moveUp(String name)
    {
        for (int i = 0; i < mods.size(); i++)
        {
            if (mods.get(i).getName().equals(name))
            {
                if (i - 1 < 0) break;

                Mod selected = mods.get(i);
                Mod above = mods.get(i - 1);
                mods.set(i, above);
                mods.set(i - 1, selected);
            }
        }
        eventBus.getSubscribers().forEach(VoidPipe::callback);
    }

    public void moveDown(String name)
    {
        for (int i = 0; i < mods.size(); i++)
        {
            if (mods.get(i).getName().equals(name))
            {
                if (i + 1 > mods.size()-1) break;

                Mod selected = mods.get(i);
                Mod below = mods.get(i + 1);
                mods.set(i, below);
                mods.set(i + 1, selected);
            }
        }
        eventBus.getSubscribers().forEach(VoidPipe::callback);
    }

    public Mod get(String name)
    {
        for (Mod mod : mods)
            if (mod.getName().equals(name))
                return mod;
        return null;
    }

    public EventBus<VoidPipe> getEventBus()
    {
        return eventBus;
    }

    public ArrayList<Mod> getMods()
    {
        return mods;
    }

    public ArrayList<Mod> getEnabledMods()
    {
        ArrayList<Mod> enabled = new ArrayList<>();
        for (Mod m : mods)
            if (m.isEnabled())
                enabled.add(m);
        return enabled;
    }
}
