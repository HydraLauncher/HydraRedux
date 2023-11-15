package org.gethydra.redux.backend.versions;

import org.gethydra.redux.backend.versions.betterjsons.BJManifest;

import java.util.ArrayList;

public class VersionFilter
{
    public ArrayList<BJManifest.BJVersionEntry> filter(ArrayList<BJManifest.BJVersionEntry> versions, boolean snapshots, boolean betas, boolean alphas)
    {
        try
        {
            ArrayList<BJManifest.BJVersionEntry> filtered = new ArrayList<>();
            for (BJManifest.BJVersionEntry v : versions)
            {
                if (v.type.equalsIgnoreCase("release")) filtered.add(v);
                else if (v.type.equalsIgnoreCase("old_beta") && betas) filtered.add(v);
                else if (v.type.equalsIgnoreCase("alpha") && alphas) filtered.add(v);
            }
            return filtered;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return new ArrayList<>();
        }
    }
}
