package com.geekhalo.redpackage.repository.impl;

import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.UserRedPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserRedPackageRepositoryImpl implements UserRedPackageRepository {
    private static final String SQL_INSERT_PRE = "insert into tb_user_red_package (user_id, activity_id, red_package_id, amount) values ";
    private static final String SQL_GET_BY_USER_ID = "select user_id, activity_id, red_package_id, amount from tb_user_red_package where user_id = ?";
    private static final String SQL_GET_BY_USER_ID_AND_ACTIVITY = "select user_id, activity_id, red_package_id, amount from tb_user_red_package where user_id = ? and activity_id = ?";

    private JdbcTemplate jdbcTemplate;

    private RowMapper<UserRedPackage> converter = (resultSet, i) -> {
        UserRedPackage userRedPackage = new UserRedPackage();
        userRedPackage.setUserId(resultSet.getString("user_id"));
        userRedPackage.setActivityId(resultSet.getString("activity_id"));
        userRedPackage.setRedPackageId(resultSet.getString("red_package_id"));
        userRedPackage.setAmount(resultSet.getInt("amount"));
        return userRedPackage;
    };

    @Autowired
    public void setDataSource(DataSource datasource) {
        this.jdbcTemplate = new JdbcTemplate(datasource);
    }

    @Override
    public void save(UserRedPackage userRedPackage) {
        String sql = SQL_INSERT_PRE + toInsertValue(userRedPackage);
        this.jdbcTemplate.execute(sql);
    }

    @Override
    public void save(List<UserRedPackage> userRedPackages) {
        String values = userRedPackages.stream()
                .map(this::toInsertValue)
                .collect(Collectors.joining(","));
        String sql = SQL_INSERT_PRE + values;
        this.jdbcTemplate.execute(sql);
    }

    @Override
    public List<UserRedPackage> getByUser(String userId) {
        return this.jdbcTemplate.query(SQL_GET_BY_USER_ID, new Object[]{userId}, this.converter);
    }

    @Override
    public List<UserRedPackage> getByUserAndActivity(String userId, String activityId) {
        return this.jdbcTemplate.query(SQL_GET_BY_USER_ID_AND_ACTIVITY, new Object[]{userId, activityId}, this.converter);
    }

    private String  toInsertValue(UserRedPackage userRedPackage) {
        return new StringBuilder().append("(")
                .append("'").append(userRedPackage.getUserId()).append("',")
                .append("'").append(userRedPackage.getActivityId()).append("',")
                .append("'").append(userRedPackage.getRedPackageId()).append("',")
                .append("'").append(userRedPackage.getAmount()).append("'")
                .append(")")
                .toString();
    }
}
