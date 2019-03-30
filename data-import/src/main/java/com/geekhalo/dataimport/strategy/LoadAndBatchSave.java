package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.repository.UserRepository;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

public class LoadAndBatchSave
        extends AbstractUserImportService
        implements UserImportService{

    private int batchSize = 50000;

    protected LoadAndBatchSave(UserParser userParser, UserRepository userRepository) {
        super(userParser, userRepository);
    }


    @Override
    public void importFromFile(File file) {
        List<String> data = Lists.newLinkedList();
        loadData(file, line -> {
            data.add(line);
            if (data.size() >= batchSize){
                batchSave(data);
                data.clear();
            }
        });
        if (!data.isEmpty()){
            batchSave(data);
        }
    }
}
