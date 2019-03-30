package com.geekhalo.redpackage.repository.impl;

import com.geekhalo.redpackage.domain.RedPackage;
import com.geekhalo.redpackage.exception.ObjectOptimisticLockingFailureException;
import com.geekhalo.redpackage.repository.RedPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Repository
public class RedPackageRepositoryImpl implements RedPackageRepository {
    private static final String SQL_INSERT_PRE = "insert into tb_red_package (id, activity_id, `version`, status, amount) values ";
    private static final String SQL_GET_BY_ID = "select id, activity_id, `version`, status, amount from tb_red_package where id = ?";
    // 别用 order by rand() limit 1 获取随机数据，效率极差
    private static final String SQL_GET_ENABLE_BY_ACTIVITY_ID = "select " +
            "id, activity_id, `version`, status, amount from tb_red_package " +
            "where activity_id = ? and status = " + RedPackage.STATUS_ENABLE +
            " limit 80";
    private static final String SQL_UPDATE = "" +
            "update tb_red_package " +
            "set " +
            "`version`=?, " +
            "status=? " +
            "where " +
            "id=? and `version`= ?";

    private static final String BATCH_DISABLE = "" +
            "update tb_red_package " +
            "set " +
            "`version` = `version` + 1, " +
            "status=" + RedPackage.STATUS_DISABLE + " " +
            "where id in (%s) and status = " + RedPackage.STATUS_ENABLE;

    private RowMapper<RedPackage> converter =  (resultSet, i) -> {
        RedPackage redPackage = new RedPackage();
        redPackage.setId(resultSet.getString("id"));
        redPackage.setActivityId(resultSet.getString("activity_id"));
        redPackage.setVersion(resultSet.getInt("version"));
        redPackage.setStatus(resultSet.getInt("status"));
        redPackage.setAmount(resultSet.getInt("amount"));
        return redPackage;
    };

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void save(RedPackage redPackage) {
        this.jdbcTemplate.execute(SQL_INSERT_PRE + toInsertValue(redPackage));
    }

    @Override
    public void save(List<RedPackage> redPackages) {
        if (CollectionUtils.isEmpty(redPackages)){
            return;
        }
        String values = redPackages.stream()
                .map(this::toInsertValue)
                .collect(Collectors.joining(","));
        this.jdbcTemplate.execute(SQL_INSERT_PRE + values);
    }


    /**
     * update tb_red_package
     * set
     *     `version`=?,
     *     status=?
     * where
     *    id=? and `version`= ?
     * @param redPackage
     */
    @Override
    public void update(RedPackage redPackage) {
        int version = redPackage.getVersion();
        int result = this.jdbcTemplate.update(SQL_UPDATE, new Object[]{
            version + 1,
                redPackage.getStatus(),
                redPackage.getId(),
                version
        });
        if (result != 1){
            throw new ObjectOptimisticLockingFailureException();
        }
    }

    private String  toInsertValue(RedPackage redPackage) {
        return new StringBuilder().append("(")
                .append("'").append(redPackage.getId()).append("',")
                .append("'").append(redPackage.getActivityId()).append("',")
                .append("'").append(redPackage.getVersion()).append("',")
                .append("'").append(redPackage.getStatus()).append("',")
                .append("'").append(redPackage.getAmount()).append("'")
                .append(")")
                .toString();
    }


    @Override
    public RedPackage getById(String id) {
        return this.jdbcTemplate.queryForObject(SQL_GET_BY_ID, new Object[]{id},converter);
    }

    @Override
    public RedPackage getEnableByActivity(String activityId) {
        List<RedPackage> redPackages = this.jdbcTemplate.query(SQL_GET_ENABLE_BY_ACTIVITY_ID, new Object[]{activityId}, this.converter);
        if (CollectionUtils.isEmpty(redPackages)){
            return null;
        }
        int size = redPackages.size();
        return redPackages.get(Math.abs(new Random().nextInt() % size));
    }

    @Override
    public void batchDisable(List<String> redPackageIds) {
        String ids = redPackageIds.stream()
                .map(id->"'" + id + "'")
                .collect(Collectors.joining(","));
        String sql = String.format(BATCH_DISABLE, ids);
        this.jdbcTemplate.execute(sql);
    }
}
