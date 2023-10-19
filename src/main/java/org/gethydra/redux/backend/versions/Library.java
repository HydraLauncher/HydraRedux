package org.gethydra.redux.backend.versions;

import java.util.HashMap;

public class Library
{
    public HashMap<String, Artifact> downloads;
    public HashMap<String, Artifact> classifiers;

    public Library() {}

    public Library(String lib)
    {
        downloads = new HashMap<>();
        classifiers = new HashMap<>();

        switch (lib)
        {
            default:
            case "lwjgl":
                Artifact lwjgl = new Artifact(
                        "org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar",
                        "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl/2.9.0/lwjgl-2.9.0.jar",
                        "5654d06e61a1bba7ae1e7f5233e1106be64c91cd",
                        994633
                );
                downloads.put("lwjgl", lwjgl);
                break;
            case "lwjgl_util":
                Artifact lwjgl_util = new Artifact(
                        "org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar",
                        "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl_util/2.9.0/lwjgl_util-2.9.0.jar",
                        "a778846b64008fc7f48ead2377f034e547991699",
                        173360
                );
                downloads.put("lwjgl_util", lwjgl_util);
                break;
            case "jinput":
                Artifact jinput = new Artifact(
                        "net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar",
                        "https://libraries.minecraft.net/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar",
                        "39c7796b469a600f72380316f6b1f11db6c2c7c4",
                        208338
                );
                downloads.put("jinput", jinput);
                break;
            case "jutils":
                Artifact jutils = new Artifact(
                        "bin/jutils.jar",
                        "https://libraries.minecraft.net/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar",
                        "e12fe1fda814bd348c1579329c86943d2cd3c6a6",
                        7508
                );
                downloads.put("jutils", jutils);
                break;
            case "natives":
                Artifact nativesLinux = new Artifact(
                        "org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-linux.jar",
                        "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-linux.jar",
                        "2ba5dcb11048147f1a74eff2deb192c001321f77",
                        569061
                );
                Artifact nativesWindows = new Artifact(
                        "org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-windows.jar",
                        "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-windows.jar",
                        "3f11873dc8e84c854ec7c5a8fd2e869f8aaef764",
                        609967
                );
                Artifact nativesMac = new Artifact(
                        "org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-osx.jar",
                        "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-osx.jar",
                        "6621b382cb14cc409b041d8d72829156a87c31aa",
                        518924
                );
                classifiers.put("natives-linux", nativesLinux);
                classifiers.put("natives-windows", nativesWindows);
                classifiers.put("natives-macos", nativesMac);
                break;
        }
    }
}
