package com.geekhalo.redpackage.util;

import java.util.UUID;

public class IDUtils {
    public static String genId(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
