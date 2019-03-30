package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.repository.UserRepository;

import java.io.File;

public class LoadAndSingleSave
        extends AbstractUserImportService
        implements UserImportService{


    protected LoadAndSingleSave(UserParser userParser, UserRepository userRepository) {
        super(userParser, userRepository);
    }

    @Override
    public void importFromFile(File file) {
        loadData(file, line -> singleSave(line));
    }
}
