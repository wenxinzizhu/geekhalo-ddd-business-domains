package com.geekhalo.dataimport.repository;

import com.geekhalo.dataimport.domain.User;

import java.util.Collection;

public interface UserRepository {
    void save(User user);

    void save(Collection<User> users);
}
