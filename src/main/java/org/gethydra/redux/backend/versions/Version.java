package org.gethydra.redux.backend.versions;

import org.gethydra.redux.Util;

import java.io.File;
import java.util.List;

public class Version
{
    public String id;
    public String type;
    public Artifact client;
    public List<Library> libraries;
    public String java_version;
    public String arguments;
    public String main_class;

    public Version() {}

    public Version(String id, String type, Artifact client, List<Library> libraries, String arguments, String main_class)
    {
        this.id = id;
        this.type = type;
        this.client = client;
        this.libraries = libraries;
        this.arguments = arguments;
        this.main_class = main_class;
    }

    public File getDirectory()
    {
        return new File(Util.getVersionsDirectory(), id);
    }

    public String toString()
    {
        return id;
    }
}
