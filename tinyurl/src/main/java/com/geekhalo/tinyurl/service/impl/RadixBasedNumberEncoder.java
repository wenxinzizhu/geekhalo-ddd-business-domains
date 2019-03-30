package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.service.NumberEncoder;
import com.geekhalo.tinyurl.service.NumberEncoder;


public class RadixBasedNumberEncoder implements NumberEncoder {
    private final int radix;
    public RadixBasedNumberEncoder(int radix){
        this.radix = radix;
    }

    @Override
    public String encode(Long id) {
        return Long.toString(id, radix);
    }

    @Override
    public Long decode(String str) {
        return Long.valueOf(str, radix);
    }
}
