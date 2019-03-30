package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.repository.impl.JpaBasedUserDao;
import com.google.common.base.Stopwatch;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Config.DATA_FILE_PATH;
import static com.geekhalo.dataimport.Config.DATA_LINE_COUNT;

abstract class AbstractUserImportServiceTest {
    @Autowired
    private Flyway flyway;

    @Autowired
    private JpaBasedUserDao userDao;

    private File file;

    protected abstract UserImportService getUserImportService();

    @Before
    public void setup(){
        flyway.clean();
        flyway.migrate();
        this.file = new File(DATA_FILE_PATH);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testImport(){
        UserImportService userImportService = getUserImportService();
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
