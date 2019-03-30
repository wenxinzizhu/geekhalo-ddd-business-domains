package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.repository.UserRepository;
import com.geekhalo.dataimport.repository.impl.JpaBasedUserDao;
import com.geekhalo.dataimport.service.impl.CommonUserParser;
import com.google.common.base.Stopwatch;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Config.DATA_FILE_PATH;
import static com.geekhalo.dataimport.Config.DATA_LINE_COUNT;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadAndBatchSaveTest1{

    @Autowired
    private UserRepository jdbcValuesBasedUserRepository;

    private LoadAndBatchSave loadAndBatchSave;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JpaBasedUserDao userDao;

    private File file;

    @Before
    public void setup(){
        flyway.clean();
        flyway.migrate();
        this.file = new File(DATA_FILE_PATH);
        this.loadAndBatchSave = new LoadAndBatchSave(new CommonUserParser(), this.jdbcValuesBasedUserRepository);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testImport(){
        UserImportService userImportService = this.loadAndBatchSave;
        Stopwatch stopwatch = Stopwatch.createStarted();
        userImportService.importFromFile(this.file);
        stopwatch.stop();
        long count = userDao.count();
        System.out.println(String.format("strategy %s save %s user cost %s ms, TPS is %f/s.",
                userImportService.getClass().getSimpleName(),
                count,
                stopwatch.elapsed(TimeUnit.MILLISECONDS),
                count * 1F / stopwatch.elapsed(TimeUnit.SECONDS)
        ));



        Assert.assertEquals(DATA_LINE_COUNT, count);
    }

}