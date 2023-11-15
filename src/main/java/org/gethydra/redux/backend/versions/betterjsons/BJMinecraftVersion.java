package org.gethydra.redux.backend.versions.betterjsons;

import org.gethydra.redux.backend.versions.Artifact;

import java.util.List;
import java.util.Map;

public class BJMinecraftVersion {
    public String releaseTime;
    public String mainClass;
    public JavaVersion javaVersion;
    public List<Library> libraries;
    public String type;
    public String assets;
    public String minecraftArguments;
    public PackageDownloads downloads;
    public int complianceLevel;
    public int minimumLauncherVersion;
    public BJAssetIndex assetIndex;
    public String id;
    public String time;

    public static class PackageDownloads
    {
        public PackageArtifact client;
        public PackageArtifact server;

        public static class PackageArtifact
        {
            public String url;
            public String sha1;
            public long size;
        }
    }

    public static class JavaVersion {
        public String component;
        public int majorVersion;
    }

    public static class Library {
        public Downloads downloads;
        public String name;
        public Extract extract;
        public Natives natives;

        public static class Downloads {
            public Artifact artifact;
            public Map<String, Artifact> classifiers; // Map to hold classifiers
        }

        public static class Classifiers {
            public Artifact sources;
        }
    }

    public static class Extract
    {
        public List<String> exclude;
    }

    public static class Natives
    {
        public String osx;
        public String linux;
        public String windows;
    }
}