package org.gethydra.redux.backend.versions;

public class Artifact
{
    public String path, sha1, url;
    public long size;

    public Artifact() {}

    public Artifact(String path, String url, String sha1, long size)
    {
        this.path = path;
        this.url = url;
        this.sha1 = sha1;
        this.size = size;
    }
}
