package com.geekhalo.redpackage.repository;

import com.geekhalo.redpackage.domain.RedPackageActivity;

public interface RedPackageActivityRepository {
    void save(RedPackageActivity activity);

    void update(RedPackageActivity activity);

    RedPackageActivity getById(String id);

}
