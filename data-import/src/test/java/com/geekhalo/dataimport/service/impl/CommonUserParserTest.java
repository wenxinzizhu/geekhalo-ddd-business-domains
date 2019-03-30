package com.geekhalo.dataimport.service.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.service.UserParser;
import com.google.common.base.Stopwatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Utils.createRandomData;

public class CommonUserParserTest {
    private static final int COUNT = 10000 * 100;
    private List<String> data;
    private UserParser userParser;

    @Before
    public void setUp() throws Exception {
        // 准备数据
        this.data = createTestData(COUNT);
        this.userParser = new CommonUserParser();
    }

    private List<String> createTestData(int count) {
        List<String> data = new ArrayList<String>(count);
        for (int i=0;i<count;i++){
            data.add(createRandomData());
        }
        return data;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void parse() throws Exception {
        this.data.forEach(line->this.userParser.parse(line));
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String line : this.data){
            User user = this.userParser.parse(line);
//            Assert.assertNotNull(user);
        }
        stopwatch.stop();
        System.out.println(String.format("parse %s data cost %s ms, TPS is %f/s",
                COUNT, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                COUNT * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

}