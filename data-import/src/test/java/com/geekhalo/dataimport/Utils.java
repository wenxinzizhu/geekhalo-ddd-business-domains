package com.geekhalo.dataimport;

import com.geekhalo.dataimport.domain.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Utils {

    public static String createRandomData(){
        return toLine(createRandomUser());
    }

    public static String toLine(User user) {
        return new StringBuilder()
                .append(user.getUid())
                .append("#")
                .append(user.getName())
                .append("#")
                .append(new SimpleDateFormat("yyyy-MM-dd").format(user.getBirthAt()))
                .toString();
    }

    public static User createRandomUser() {
        Random random = new Random();
        return User.builder()
                .uid(Math.abs(random.nextLong()))
                .name("name-" + Math.abs(random.nextInt()))
                .birthAt(new Date())
                .build();
    }
}
