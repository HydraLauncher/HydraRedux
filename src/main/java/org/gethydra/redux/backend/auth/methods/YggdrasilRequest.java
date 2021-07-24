package org.gethydra.redux.backend.auth.methods;

public class YggdrasilRequest
{
    public Agent agent;
    public String username;
    public String password;
    public String clientToken;

    public YggdrasilRequest(String username, String password)
    {
        this.agent = Agent.Minecraft;
        this.username = username;
        this.password = password;
    }

    private static class Agent
    {
        public static final Agent Minecraft = new Agent("Minecraft", 1);

        public String name;
        public int version;

        public Agent(String name, int version)
        {
            this.name = name;
            this.version = version;
        }
    }
}