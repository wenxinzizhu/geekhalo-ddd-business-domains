package com.geekhalo.tinyurl.application;

public interface TinyUrlApplication {
    String shortUrl(String strategy, String url);

    String getTargetUrl(String request);
}
