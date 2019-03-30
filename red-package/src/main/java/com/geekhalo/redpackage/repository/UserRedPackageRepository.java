package com.geekhalo.redpackage.repository;

import com.geekhalo.redpackage.domain.UserRedPackage;

import java.util.List;

public interface UserRedPackageRepository {
    void save(UserRedPackage userRedPackage);

    void save(List<UserRedPackage> userRedPackages);

    List<UserRedPackage> getByUser(String userId);

    List<UserRedPackage> getByUserAndActivity(String userId, String activityId);
}
