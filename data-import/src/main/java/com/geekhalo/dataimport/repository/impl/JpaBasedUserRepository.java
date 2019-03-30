package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;


@Service
public class JpaBasedUserRepository implements UserRepository {
    @Autowired
    private JpaBasedUserDao userDao;

    @Override
    public void save(User user) {
        this.userDao.save(user);
    }

    @Override
    public void save(Collection<User> users) {
        this.userDao.saveAll(users);
    }
}
