package com.geekhalo.tinyurl.service.encoder;

import com.geekhalo.tinyurl.service.NumberEncoder;
import com.geekhalo.tinyurl.service.impl.RadixBasedNumberEncoder;
import com.geekhalo.tinyurl.service.impl.ShortUrlUtils;
import com.geekhalo.tinyurl.service.NumberEncoder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class NumberEncoderTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConvert() {
        NumberEncoder numberEncoder = new RadixBasedNumberEncoder(35);
        Random random = new Random();
        for (int i=0; i<10000;i++){
            Long number = Math.abs(random.nextLong());
            String str = numberEncoder.encode(number);
            // 检测编解码后，是否一致
            Assert.assertEquals(number, numberEncoder.decode(str));
        }
    }

}