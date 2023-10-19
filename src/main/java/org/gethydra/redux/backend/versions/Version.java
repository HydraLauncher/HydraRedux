package org.gethydra.redux.backend.versions;

import org.gethydra.redux.Util;

import java.io.File;
import java.util.ArrayList;
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

    public Version(boolean test)
    {
        this.id = "b1.7.3";
        this.type = "release";
        this.client = new Artifact(
                "bin/minecraft",
                "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar",
                "43db9b498cb67058d2e12d394e6507722e71bb45",
                1465375
        );
        this.libraries = new ArrayList<>();
        this.libraries.add(new Library("lwjgl"));
        this.libraries.add(new Library("lwjgl_util"));
        this.libraries.add(new Library("jinput"));
        this.libraries.add(new Library("jutils"));
        this.libraries.add(new Library("natives"));
        this.java_version = "8";
        this.arguments = "${username} ${accessToken}";
        this.main_class = "net.minecraft.client.Minecraft";
    }

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
        File file = new File(Util.getVersionsDirectory(), id);
        if (!file.exists()) file.mkdirs();
        return file;
    }

    public String toString()
    {
        return id;
    }
}
