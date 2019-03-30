package com.geekhalo.dataimport.service.impl;

import com.geekhalo.dataimport.service.FileDataLoader;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.*;

public final class StreamBasedFileDataLoader implements FileDataLoader {

    @Override
    public void load(File file, LineCallback lineCallback){
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = Files.newReader(file, Charsets.UTF_8);
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                // 每成功读取一行数据，就通过lineCallback回调进行数据处理
                lineCallback.onData(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
