package org.gethydra.redux.backend;

import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;
import org.gethydra.redux.backend.profiles.ProfileManager;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class TokenProcessor
{
    private final String input;

    public TokenProcessor(String input)
    {
        this.input = input;
    }

    public String[] process()
    {
        HashMap<String, String> dataMap = new HashMap<>();
        DataStore store = HydraRedux.getInstance().getDataStore();
        ProfileManager pm = HydraRedux.getInstance().getProfileManager();
        dataMap.put("${auth_player_name}", store.getString("username", "Player" + new Random().nextInt(999)));
        dataMap.put("${auth_session}", store.getString("accessToken", "1337"));
        dataMap.put("${game_directory}", new File(pm.getSelectedProfile().getGameDirectory()).getAbsolutePath().replace(" ", "_"));
        dataMap.put("${game_assets}", store.getString("assetsDirectory", new File(pm.getSelectedProfile().getGameDirectory(), "resources").getAbsolutePath()));
        
        String input = this.input;
        for (String key : dataMap.keySet())
        {
            String value = dataMap.get(key);
            input = input.replace(key, value);
        }

        return input.trim().split(" ");
    }
}
