package com.geekhalo.tinyurl.repository.impl;

import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.geekhalo.tinyurl.repository.impl.dao.TargetUrlDao;
import com.geekhalo.tinyurl.domain.TargetUrl;
import com.geekhalo.tinyurl.repository.TargetUrlRepository;
import com.geekhalo.tinyurl.repository.impl.dao.TargetUrlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("dbBasedTinyUrlRepository")
public class DBBasedTargetUrlRepository implements TargetUrlRepository {
    @Autowired
    private TargetUrlDao targetUrlDao;

    @Override
    public String getStrategyName() {
        return "d";
    }

    @Override
    public void save(TargetUrl targetUrl) {
        this.targetUrlDao.save(targetUrl);
    }


    @Override
    public TargetUrl getById(Long id) {
        return this.targetUrlDao.findById(id).orElse(null);
    }
}
