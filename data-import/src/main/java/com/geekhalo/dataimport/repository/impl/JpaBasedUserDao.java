package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBasedUserDao extends JpaRepository<User, Long> {
}
