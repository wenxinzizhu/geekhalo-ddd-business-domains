package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.domain.UserRedPackage;
import com.geekhalo.redpackage.repository.UserRedPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AbstractRedPackageAppliction {
    @Autowired
    protected UserRedPackageRepository userRedPackageRepository;

    public List<UserRedPackage> getByUser(String userId) {
        return userRedPackageRepository.getByUser(userId);
    }

    public List<UserRedPackage> getByUserAndActivity(String userId, String activity) {
        return userRedPackageRepository.getByUserAndActivity(userId, activity);
    }
}
