package com.geekhalo.tinyurl.controller;

import com.geekhalo.tinyurl.application.TinyUrlApplication;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ShortController {
    @Autowired
    private TinyUrlApplication tinyUrlApplication;

    @PostMapping("short-url")
    public String create(@RequestBody ShortUrlForm form){
        return tinyUrlApplication.shortUrl(form.getStrategy(), form.getUrl());
    }

    @Data
    static class ShortUrlForm{
        private String strategy;
        private String url;
    }
}
