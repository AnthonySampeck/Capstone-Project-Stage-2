package com.example.newonnetflix.remote;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {

        url = new URL("http://instantwatcher.com/new?content_type=1+2&sort=available_from+desc");


        } catch (MalformedURLException ignored) {

        }

        BASE_URL = url;
    }
}
