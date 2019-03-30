package com.geekhalo.dataimport.service;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.service.impl.CommonUserParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonUserParserTest {
    @Autowired
    private CommonUserParser userParser;
    private String data = "123#testName#1985-10-25";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parse() {
        User user = this.userParser.parse(this.data);
        Assert.assertEquals(123L, user.getUid().longValue());
        Assert.assertEquals("testName", user.getName());
        Assert.assertEquals("1985-10-25", new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthAt()));
    }
}