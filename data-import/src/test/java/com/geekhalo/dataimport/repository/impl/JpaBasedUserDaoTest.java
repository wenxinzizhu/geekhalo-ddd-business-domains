package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaBasedUserDaoTest {

    @Autowired
    private JpaBasedUserDao userDao;

    private List<User> users;

    @Before
    public void setUp() throws Exception {
        this.users = Lists.newArrayList();
        for (int i=0;i<10;i++){
            this.users.add(createRandomUser());
        }
        this.userDao.saveAll(this.users);


    }

    @After
    public void tearDown() throws Exception {
        this.users.forEach(user -> this.userDao.deleteById(user.getId()));
    }


    @Test
    public void saveTest(){
        for (int i=0; i<10;i++){
            User user =createRandomUser();
            this.users.add(user);
            this.userDao.save(user);
            Assert.assertNotNull(user.getId());
        }
    }

    @Test
    public void saveAllTest(){
        this.users.forEach(user -> Assert.assertNotNull(user.getId()));
    }

    @Test
    public void getByIdTest(){
        this.users.forEach(user -> {
            User u = this.userDao.getOne(user.getId());
            Assert.assertNotNull(u);
        });
    }

    private User createRandomUser() {
        Random random = new Random();
        return User.builder()
                .uid(random.nextLong())
                .name("name-" + random.nextInt())
                .birthAt(new Date())
                .build();
    }


}