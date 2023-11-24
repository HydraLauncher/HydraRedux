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
        if (System.getenv().containsKey("HYDRA_DEBUG")) Thread.dumpStack();
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
            String version_data_raw = Util.get(url);
            if (System.getenv().containsKey("HYDRA_DEBUG")) System.out.println(version_data_raw);
            return gson.fromJson(version_data_raw, BJMinecraftVersion.class);
        }

        @Override
        public String toString()
        {
            return id;
        }
    }
}
