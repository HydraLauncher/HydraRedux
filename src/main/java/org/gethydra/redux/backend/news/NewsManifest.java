package org.gethydra.redux.backend.news;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.util.ArrayList;

public class NewsManifest
{
    private static final Gson gson = new Gson();

    private ArrayList<NewsEntry> news;

    public ArrayList<NewsEntry> getEntries()
    {
        return news;
    }

    public NewsManifest(boolean defVals)
    {
        news = new ArrayList<>();
        NewsEntry newsEntry = new NewsEntry();
        newsEntry.title = "News broke :(";
        newsEntry.lines = new String[] { "The API is no longer with us.", "Please observe a moment of silence for the News API." };
        news.add(newsEntry);
    }

    public static NewsManifest fetch()
    {
        try
        {
//            String postEndpoint = "https://api.gethydra.org/news/get_news?filter=ALL";
//            CloseableHttpClient httpclient = HttpClients.createDefault();
//            HttpGet httpPost = new HttpGet(postEndpoint);
//
//            httpPost.setHeader("Accept", "application/json");
//
//            HttpResponse response = httpclient.execute(httpPost);
//            if (response.getStatusLine().getStatusCode() == 200)
//            {
//                HttpEntity responseEntity = response.getEntity();
//                InputStream is = responseEntity.getContent();
//                String contentEncoding = "UTF-8";
//                String responseContent = IOUtils.toString(is, contentEncoding);
//                return gson.fromJson(responseContent, NewsManifest.class);
//            }
            return new NewsManifest(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
