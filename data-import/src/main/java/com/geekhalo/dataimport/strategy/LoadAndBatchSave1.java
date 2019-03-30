package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.repository.UserRepository;
import com.geekhalo.dataimport.service.FileDataLoader;
import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.service.impl.StreamBasedFileDataLoader;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class LoadAndBatchSave1
        implements UserImportService{
    private final FileDataLoader fileDataLoader = new StreamBasedFileDataLoader();
    private final UserParser userParser;
    private final UserRepository userRepository;

    private int batchSize = 50000;

    protected LoadAndBatchSave1(UserParser userParser, UserRepository userRepository) {
        this.userParser = userParser;
        this.userRepository = userRepository;
    }


    @Override
    public void importFromFile(File file) {
        List<String> data = Lists.newLinkedList();
        fileDataLoader.load(file, line -> {
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

    protected void batchSave(List<String> data){
        List<User> users = data.stream()
                .map(this.userParser::parse)
                .collect(Collectors.toList());
        this.userRepository.save(users);
    }
}
