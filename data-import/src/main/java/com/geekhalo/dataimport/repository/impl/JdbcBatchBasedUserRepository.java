package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class JdbcBatchBasedUserRepository implements UserRepository{
    private static final String SQL_INSERT = "insert into tb_user (uid, name, birth_at) values (:uid, :name, :birthAt)";
    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init(){
        this.jdbcTemplate = new NamedParameterJdbcTemplate(this.dataSource);
    }

    @Override
    @Transactional
    public void save(User user) {
        this.jdbcTemplate.update(SQL_INSERT, new BeanPropertySqlParameterSource(user));
    }

    @Override
    @Transactional
    public void save(Collection<User> users) {
        int preBatchSize = 1000;
        List<List<User>> batchUser = new ArrayList<List<User>>();
        List<User> tmp = new ArrayList<>(preBatchSize);
        for (User user : users){
            tmp.add(user);
            if (tmp.size() >= preBatchSize){
                batchUser.add(tmp);
                tmp = new ArrayList<>(preBatchSize);
            }
        }
        if (!tmp.isEmpty()){
            batchUser.add(tmp);
        }
        batchUser.forEach(users1 -> {
            SqlParameterSource[] sqlParameterSources = SqlParameterSourceUtils.createBatch(users1);
            jdbcTemplate.batchUpdate(SQL_INSERT, sqlParameterSources);
        });
    }
}
