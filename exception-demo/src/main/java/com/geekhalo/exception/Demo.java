package com.geekhalo.exception;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Demo {
    public static void main(String... arg){
        try {
            // 正常处理流程，正确执行过程做什么事
            Path path = Paths.get("var", "error");
            List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
            System.out.println(lines);
        } catch (IOException e) {
            // 异常处理流程，出了问题怎么办
            e.printStackTrace();
        }
    }
}
