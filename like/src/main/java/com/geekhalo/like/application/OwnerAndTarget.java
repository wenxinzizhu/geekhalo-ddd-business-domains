package com.geekhalo.like.application;


import com.geekhalo.like.domain.Owner;
import com.geekhalo.like.domain.Target;
import lombok.Data;

@Data
public class OwnerAndTarget {
    private Owner owner;
    private Target target;
}
