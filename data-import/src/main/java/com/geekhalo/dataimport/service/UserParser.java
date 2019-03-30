package com.geekhalo.dataimport.service;

import com.geekhalo.dataimport.domain.User;

public interface UserParser {
    User parse(String line);
}
