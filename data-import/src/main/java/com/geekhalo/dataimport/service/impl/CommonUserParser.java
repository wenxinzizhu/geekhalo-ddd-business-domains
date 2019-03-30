package com.geekhalo.dataimport.service.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.service.UserParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUserParser implements UserParser {

    @Override
    public User parse(String line){
        String[] ss = line.split("#");
        if (ss.length != 3){
            return null;
        }
        return User.builder()
                .uid(Long.valueOf(ss[0]))
                .name(ss[1])
                .birthAt(getDate(ss[2]))
                .build();
    }

    private Date getDate(String s) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
