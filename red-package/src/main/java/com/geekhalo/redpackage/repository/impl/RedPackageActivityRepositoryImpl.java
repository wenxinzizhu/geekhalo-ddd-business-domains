package com.geekhalo.redpackage.repository.impl;

import com.geekhalo.redpackage.domain.RedPackageActivity;
import com.geekhalo.redpackage.exception.ObjectOptimisticLockingFailureException;
import com.geekhalo.redpackage.repository.RedPackageActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class RedPackageActivityRepositoryImpl implements RedPackageActivityRepository {
    private static final String SQL_INSERT_PRE = "insert into tb_red_package_activity (id, total_amount, total_number, `version`, surplus_amount, surplus_number) values ";
    private static final String SQL_GET_BY_ID = "select id, total_amount, total_number, `version`, surplus_amount, surplus_number from tb_red_package_activity where id = ?";
    private static final String SQL_UPDATE_BY_ID = "" +
            "update tb_red_package_activity " +
            "set " +
            "`version`=?, " +
            "surplus_amount=?, " +
            "surplus_number=? " +
            "where id = ? and `version` = ?";


    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource datasource) {
        this.jdbcTemplate = new JdbcTemplate(datasource);
    }

    @Override
    public void save(RedPackageActivity activity) {
        String sql = SQL_INSERT_PRE + toInsertValue(activity);
        this.jdbcTemplate.execute(sql);
    }

    @Override
    public void update(RedPackageActivity activity) {
        // 获取 activity 读取时的 version
        int version = activity.getVersion();

        // update tb_red_package_activity
        // set
        //   `version`=?,
        //    surplus_amount=?,
        //    surplus_number=?
        // where id = ? and `version` = ?
        // 根据读取时的 version 执行更新操作
        // result == 1，执行成功，说明从读取到保存这段时间，数据库中的数据没有改变
        // result == 0，执行失败，说明从读取到保存这段时间，数据库中的数据已经被其他操作改变
        int result = this.jdbcTemplate.update(SQL_UPDATE_BY_ID, new Object[]{
                version + 1, // 更新version
                activity.getSurplusAmount(),
                activity.getSurplusNumber(),
                activity.getId(),
                version
        });

        // 执行失败时，抛出异常，以触发事务回滚
        if (result != 1){
            throw new ObjectOptimisticLockingFailureException();
        }
    }

    @Override
    public RedPackageActivity getById(String id) {
        return this.jdbcTemplate.queryForObject(SQL_GET_BY_ID, new Object[]{id}, (resultSet, i) -> {
            RedPackageActivity redPackageActivity = new RedPackageActivity();
            redPackageActivity.setId(resultSet.getString("id"));
            redPackageActivity.setTotalAmount(resultSet.getInt("total_amount"));
            redPackageActivity.setTotalNumber(resultSet.getInt("total_number"));

            redPackageActivity.setVersion(resultSet.getInt("version"));
            redPackageActivity.setSurplusAmount(resultSet.getInt("surplus_amount"));
            redPackageActivity.setSurplusNumber(resultSet.getInt("surplus_number"));
            return redPackageActivity;
        });
    }

    private String  toInsertValue(RedPackageActivity redPackageActivity) {
        return new StringBuilder().append("(")
                .append("'").append(redPackageActivity.getId()).append("',")
                .append("'").append(redPackageActivity.getTotalAmount()).append("',")
                .append("'").append(redPackageActivity.getTotalNumber()).append("',")
                .append("'").append(redPackageActivity.getVersion()).append("',")
                .append("'").append(redPackageActivity.getSurplusAmount()).append("',")
                .append("'").append(redPackageActivity.getSurplusNumber()).append("'")
                .append(")")
                .toString();
    }
}
