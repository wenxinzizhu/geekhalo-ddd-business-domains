package com.geekhalo.tinyurl.repository;

import com.geekhalo.tinyurl.domain.NumberGen;
import com.geekhalo.tinyurl.domain.NumberType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NumberGenRepository extends JpaRepository<NumberGen, Long> {
    NumberGen getByType(NumberType type);
}
