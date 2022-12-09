package com.pzr.xls2jd;

import com.pzr.xls2jd.core.domain.CompanyProperties;
import com.pzr.xls2jd.core.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Xls2jdApplicationTests {
 @Autowired
    CompanyProperties properties;
 @Autowired
    ProcessorService processorService;
    @Test
    void contextLoads() {
    }

    @Test
   public void test1(){

        System.out.println(processorService.isContainKeyWord("利息", "bankInterest"));
        System.out.println(processorService.isContainKeyWord("结息", "bankInterest"));
        System.out.println(processorService.isContainKeyWord("结1息", "bankInterest"));

    }
}
