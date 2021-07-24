package org.gethydra.redux;

public class MinecraftServerAddress
{
    public String hostname;
    public short port;

    public MinecraftServerAddress() {}

    public MinecraftServerAddress(String hostname, short port)
    {
        this.hostname = hostname;
        this.port = port;
    }

    public String full()
    {
        return hostname + ":" + port;
    }
}
