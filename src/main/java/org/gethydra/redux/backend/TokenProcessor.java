package org.gethydra.redux.backend;

import org.gethydra.redux.HydraRedux;

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
        input = input.replace("${username}", store.getString("username", "Steve"));
        input = input.replace("${accessToken}", store.getString("accessToken", "1337"));
        return input;
    }
}
