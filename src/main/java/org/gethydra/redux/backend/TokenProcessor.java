package org.gethydra.redux.backend;

import org.gethydra.redux.HydraRedux;
import org.gethydra.redux.Util;

import java.util.Objects;

public class TokenProcessor
{
    private final String input;

    public TokenProcessor(String input)
    {
        this.input = input;
    }

    public String process()
    {
        DataStore store = HydraRedux.getInstance().getDataStore();
        String input = this.input;
        input = input.replace("${auth_player_name}", store.getString("username", "Steve"));
        input = input.replace("${auth_session}", store.getString("accessToken", "1337"));
        input = input.replace("${game_directory}", store.getString("gameDirectory", Objects.requireNonNull(Util.getHydraDirectory()).getAbsolutePath()));
        input = input.replace("${game_assets}", store.getString("assetsDirectory", "1337"));

        return input;
    }
}
