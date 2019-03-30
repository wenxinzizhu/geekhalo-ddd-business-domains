package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.repository.UserRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JpaBasedUserRepositoryTest extends AbstractUserRepositoryTest{

    @Autowired
    private JpaBasedUserRepository userRepository;
    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }
}