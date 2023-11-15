package org.gethydra.redux.backend.versions.betterjsons;

import com.google.gson.Gson;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.versions.Version;

import java.util.ArrayList;

public class BJManifest
{
    private static final Gson gson = new Gson();

    public ArrayList<BJVersionEntry> versions;

    public BJVersionEntry find(String id)
    {
        for (BJVersionEntry v : versions)
            if (v.id.equals(id))
                return v;
        return null;
    }

    public static class BJVersionEntry
    {
        public String releaseTime;
        public String id;
        public String time;
        public String type;
        public String url;

        public BJMinecraftVersion fetch()
        {
            return gson.fromJson(Util.get(url), BJMinecraftVersion.class);
        }

        @Override
        public String toString()
        {
            return id;
        }
    }
}
