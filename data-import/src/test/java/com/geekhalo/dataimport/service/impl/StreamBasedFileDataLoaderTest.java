package com.geekhalo.dataimport.service.impl;

import com.geekhalo.dataimport.service.FileDataLoader;
import com.google.common.base.Stopwatch;
import lombok.Data;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Config.DATA_FILE_PATH;
import static com.geekhalo.dataimport.Config.DATA_LINE_COUNT;


public class StreamBasedFileDataLoaderTest {
    private StreamBasedFileDataLoader fileDataLoader;
    private File file;
    private LineCount lineCount;

    @Before
    public void setUp() throws Exception {
        this.file = new File(DATA_FILE_PATH);
        this.lineCount = new LineCount();
        this.fileDataLoader = new StreamBasedFileDataLoader();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void load() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        this.fileDataLoader.load(this.file, this.lineCount);
        stopWatch.stop();
        System.out.println(String.format("load %s data cost %s ms, TPS is %f/s",
                this.lineCount.getCount(), stopWatch.elapsed(TimeUnit.MILLISECONDS),
                this.lineCount.getCount() * 1f / stopWatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
        Assert.assertEquals(DATA_LINE_COUNT, this.lineCount.getCount());
    }

    /**
     * 用于记录记录总数，单线程调用不存在并发问题
     */
    @Data
    public class LineCount implements FileDataLoader.LineCallback {
        private long count = 0;

        @Override
        public void onData(String line) {
            count ++;
        }
    }
}