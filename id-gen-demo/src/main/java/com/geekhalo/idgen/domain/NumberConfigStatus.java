package com.geekhalo.idgen.domain;

public enum NumberConfigStatus {
    NORMAL("正常"),
    NO_QUOTA("没有配额"),
    DISABLE("禁用");

    private final String descr;

    NumberConfigStatus(String descr) {
        this.descr = descr;
    }

    public String getDescr() {
        return descr;
    }
}
