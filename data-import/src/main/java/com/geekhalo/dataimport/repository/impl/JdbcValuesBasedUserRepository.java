package com.geekhalo.dataimport.repository.impl;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class JdbcValuesBasedUserRepository implements UserRepository{
    private static final String SQL_INSERT_PRE = "insert into tb_user (uid, name, birth_at) values ";

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;



    @PostConstruct
    public void init(){
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @Override
    @Transactional
    public void save(User user) {
        this.jdbcTemplate.update(SQL_INSERT_PRE + getInsertValues(Arrays.asList(user)));
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

        batchUser.forEach(users1 -> this.jdbcTemplate.execute(SQL_INSERT_PRE + getInsertValues(users1)));
    }

    private String getInsertValues(List<User> users){

        return users.stream()
                .map(this::toInsertValue)
                .collect(Collectors.joining(","));
    }

    private String toInsertValue(User user){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return new StringBuilder().append("(")
                .append("'").append(user.getUid()).append("',")
                .append("'").append(user.getName()).append("',")
                .append("'").append(dateFormat.format(user.getBirthAt())).append("'")
                .append(")")
                .toString();
    }
}
