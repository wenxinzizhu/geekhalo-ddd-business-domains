package com.geekhalo.redpackage.repository;

import com.geekhalo.redpackage.domain.RedPackage;

import java.util.List;

public interface RedPackageRepository {

    void save(RedPackage redPackage);

    void save(List<RedPackage> redPackages);

    void update(RedPackage redPackage);

    RedPackage getById(String id);

    RedPackage getEnableByActivity(String activityId);

    void batchDisable(List<String> redPackageIds);
}
