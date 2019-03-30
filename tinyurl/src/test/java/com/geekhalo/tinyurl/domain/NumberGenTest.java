package com.geekhalo.tinyurl.domain;

import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Test;


import java.util.Set;


public class NumberGenTest {

    @Test
    public void nextNumber() {
        NumberGen numberGen = new NumberGen(NumberType.TINY_URL);
        Set<Long> ids = Sets.newTreeSet();
        for (int i=0;i<100;i++){
            ids.add(numberGen.nextNumber());
        }
        Assert.assertEquals(100, ids.size());

        ids = Sets.newTreeSet();
        for (int i=0;i<100;i++){
            ids.addAll(numberGen.nextNumber(100));
        }

        Assert.assertEquals(100 * 100, ids.size());

    }
}