package com.geekhalo.dataimport.strategy;

import com.geekhalo.dataimport.repository.UserRepository;
import com.geekhalo.dataimport.service.impl.CommonUserParser;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadAndConcurrentBatchSaveTest extends AbstractUserImportServiceTest{
    @Autowired
    private UserRepository jdbcValuesBasedUserRepository;

    private LoadAndConcurrentBatchSave loadAndConcurrentBatchSave;

    @Before
    public void setup(){
        super.setup();
        this.loadAndConcurrentBatchSave = new LoadAndConcurrentBatchSave(new CommonUserParser(), this.jdbcValuesBasedUserRepository);
    }

    @Override
    protected UserImportService getUserImportService() {
        return this.loadAndConcurrentBatchSave;
    }
}