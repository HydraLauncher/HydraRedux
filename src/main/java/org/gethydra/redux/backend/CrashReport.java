package org.gethydra.redux.backend;

import java.util.ArrayList;

public class CrashReport
{
    private ArrayList<StackTraceElement> elements;

    private String operatingSystem;
    private String javaVersion;

    public CrashReport(String operatingSystem, String javaVersion)
    {
        this.elements = new ArrayList<>();
        this.operatingSystem = operatingSystem;
        this.javaVersion = javaVersion;
    }

    public void addStackTraceElement(StackTraceElement ste)
    {
        elements.add(ste);
    }

    public ArrayList<StackTraceElement> getElements()
    {
        return elements;
    }
}
