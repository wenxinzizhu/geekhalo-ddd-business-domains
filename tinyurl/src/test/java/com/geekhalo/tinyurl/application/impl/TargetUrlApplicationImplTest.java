package com.geekhalo.tinyurl.application.impl;

import com.geekhalo.tinyurl.application.TinyUrlApplication;
import com.geekhalo.tinyurl.application.TinyUrlApplication;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TargetUrlApplicationImplTest {
    @Autowired
    private TinyUrlApplication tinyUrlApplication;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shortUrl() {
        Random random = new Random();
        for (int i=0;i<100;i++) {
            String url = "http://tinyurl.com/" + Math.abs(random.nextInt());
            String code = this.tinyUrlApplication.shortUrl("l", url);
            String url2 = this.tinyUrlApplication.getTargetUrl(code);
            Assert.assertEquals(url, url2);
        }
    }
}