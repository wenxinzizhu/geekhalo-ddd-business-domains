package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import com.google.common.base.Stopwatch;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.geekhalo.dataimport.Utils.createRandomUser;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaBasedUserRepositoryTest1 {
    private static final int COUNT = 10000 * 10;
    private static final int PRE_BATCH_COUNT = 2000;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JpaBasedUserRepository jpaBasedUserRepository;

    private List<User> users;

    private List<List<User>> batchUser;

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
        this.users.forEach(this.jpaBasedUserRepository::save);
        stopwatch.stop();
        System.out.println(String.format("single save %s user cost %s ms, TPS is %f/s",
                COUNT, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                COUNT * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

    @Test
    public void save1() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        this.batchUser.forEach(this.jpaBasedUserRepository::save);
        stopwatch.stop();
        System.out.println(String.format("batch save %s user cost %s ms, TPS is %f/s",
                COUNT, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                COUNT * 1f / stopwatch.elapsed(TimeUnit.MILLISECONDS)  * 1000));
    }

}