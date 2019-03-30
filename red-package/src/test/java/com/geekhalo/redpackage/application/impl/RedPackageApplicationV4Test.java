package com.geekhalo.redpackage.application.impl;

import com.geekhalo.redpackage.application.RedPackageApplication;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class RedPackageApplicationV4Test extends AbstractRedPackageApplicationTest {

    @Autowired
    private RedPackageApplicationV4 redPackageApplication;

    @Override
    protected RedPackageApplication getRedPackageApplication() {
        return redPackageApplication;
    }

    @Override
    protected void postTest(){
//        try {
//            TimeUnit.SECONDS.sleep(30);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}