package org.gethydra.redux.backend.versions;

import java.util.ArrayList;

public class VersionFilter
{
    public ArrayList<Version> filter(ArrayList<Version> versions, boolean snapshots, boolean betas, boolean alphas)
    {
        try
        {
            ArrayList<Version> filtered = new ArrayList<>();
            for (Version v : versions)
            {
                if (v.type.equalsIgnoreCase("release")) filtered.add(v);
                else if (v.type.equalsIgnoreCase("old_beta") && betas) filtered.add(v);
                else if (v.type.equalsIgnoreCase("alpha") && alphas) filtered.add(v);
            }
            return filtered;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}
