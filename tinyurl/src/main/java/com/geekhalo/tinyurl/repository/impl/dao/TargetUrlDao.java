package com.geekhalo.tinyurl.repository.impl.dao;

import com.geekhalo.tinyurl.domain.TargetUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetUrlDao extends JpaRepository<TargetUrl, Long> {
}
