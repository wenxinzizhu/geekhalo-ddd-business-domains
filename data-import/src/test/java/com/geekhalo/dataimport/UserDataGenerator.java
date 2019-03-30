package com.geekhalo.dataimport;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import static com.geekhalo.dataimport.Config.DATA_FILE_PATH;
import static com.geekhalo.dataimport.Config.DATA_LINE_COUNT;
import static com.geekhalo.dataimport.Utils.createRandomData;

public class UserDataGenerator {

    @Test
    public void generate() {
        Long lineSize = DATA_LINE_COUNT;
        String filePath = DATA_FILE_PATH;
        try {
            BufferedWriter bufferedWriter = Files.newWriter(new File(filePath), Charsets.UTF_8);
            Long dataSize = lineSize;
            while (--dataSize >=0){
                String line = createRandomData();
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                if (dataSize % 1000000 == 0){
                    bufferedWriter.flush();
                    System.out.println(dataSize);
                }

            }
            bufferedWriter.flush();
            bufferedWriter.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
