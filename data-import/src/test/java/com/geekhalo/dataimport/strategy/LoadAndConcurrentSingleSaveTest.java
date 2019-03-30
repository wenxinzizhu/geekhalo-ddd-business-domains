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
public class LoadAndConcurrentSingleSaveTest extends AbstractUserImportServiceTest{
    @Autowired
    private UserRepository jdbcBasedUserRepository;

    private LoadAndConcurrentSingleSave loadAndConcurrentSingleSave;

    @Before
    public void setup(){
        super.setup();
        this.loadAndConcurrentSingleSave = new LoadAndConcurrentSingleSave(new CommonUserParser(), jdbcBasedUserRepository);
    }

    @Override
    protected UserImportService getUserImportService() {
        return this.loadAndConcurrentSingleSave;
    }
}