package org.gethydra.redux.backend;

public enum LauncherVisibility
{
    CLOSE("Close launcher when game starts"), KEEP_OPEN("Keep the launcher open"), HIDE_THEN_OPEN("Hide launcher and re-open when game closes");

    LauncherVisibility(String description)
    {
        this.description = description;
    }

    private final String description;

    public String toString()
    {
        return description;
    }
}
