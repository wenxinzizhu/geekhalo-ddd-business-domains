package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.domain.User;
import com.geekhalo.dataimport.service.FileDataLoader;
import com.geekhalo.dataimport.service.UserParser;
import com.geekhalo.dataimport.repository.UserRepository;
import com.geekhalo.dataimport.service.impl.StreamBasedFileDataLoader;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

abstract class AbstractUserImportService {
    private final FileDataLoader fileDataLoader = new StreamBasedFileDataLoader();
    private final UserParser userParser;
    private final UserRepository userRepository;

    protected AbstractUserImportService(UserParser userParser, UserRepository userRepository) {
        this.userParser = userParser;
        this.userRepository = userRepository;
    }

    protected void loadData(File file, FileDataLoader.LineCallback lineCallback){
        fileDataLoader.load(file, lineCallback);
    }

    public UserParser getUserParser(){
        return this.userParser;
    }

    protected UserRepository getUserRepository(){
        return this.userRepository;
    }

    protected void batchSave(List<String> data){
        List<User> users = data.stream()
                .map(this.getUserParser()::parse)
                .collect(Collectors.toList());
        this.getUserRepository().save(users);
    }

    protected class BatchSaveTask implements Runnable{
        private final List<String> datas;

        public BatchSaveTask(List<String> datas) {
            this.datas = Lists.newLinkedList(datas);
        }

        @Override
        public void run() {
            batchSave(datas);
        }
    }

    protected void singleSave(String line){
        User user = this.getUserParser().parse(line);
        this.getUserRepository().save(user);
    }

    class SingleSaveTask implements Runnable{
        private final String line;

        SingleSaveTask(String line) {
            this.line = line;
        }

        @Override
        public void run() {
           singleSave(line);
        }
    }
}
