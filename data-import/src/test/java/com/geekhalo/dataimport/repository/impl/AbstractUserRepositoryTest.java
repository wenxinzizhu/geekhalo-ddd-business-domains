package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.repository.UserRepository;
import com.google.common.base.Stopwatch;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Utils.createRandomUser;


abstract class AbstractUserRepositoryTest {
    private static final int COUNT = 10000 * 10;
    private static final int PRE_BATCH_COUNT = 50000;
    @Autowired
    private Flyway flyway;

    private List<User> users;
    private List<List<User>> batchUser;

    protected abstract UserRepository getUserRepository();
    @Before
    public void setUp() throws Exception {
        this.users = new ArrayList<>(COUNT);
        this.batchUser = new ArrayList<>(COUNT/PRE_BATCH_COUNT);
        List<User> preBatch = new ArrayList<>(PRE_BATCH_COUNT);
        this.batchUser.add(preBatch);
        for (int i = 0; i< COUNT; i++){
            User user = createRandomUser();
            this.users.add(user);
            preBatch.add(user);
            if (preBatch.size()>=PRE_BATCH_COUNT){
                preBatch = new ArrayList<>(PRE_BATCH_COUNT);
                this.batchUser.add(preBatch);
            }
        }
        this.flyway.clean();
        this.flyway.migrate();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void save() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.users.forEach(this.getUserRepository()::save);
        stopwatch.stop();
        System.out.println(String.format("%s single save %s user cost %s ms, TPS is %f/s",
                getUserRepository().getClass().getSimpleName(),
                COUNT, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                COUNT * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

    @Test
    public void save1() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.batchUser.forEach(this.getUserRepository()::save);
        stopwatch.stop();
        System.out.println(String.format("%s batch save %s user cost %s ms, TPS is %f/s",
                getUserRepository().getClass().getSimpleName(),
                COUNT, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                COUNT * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }
}
