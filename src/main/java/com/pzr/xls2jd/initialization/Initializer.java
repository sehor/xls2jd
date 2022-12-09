package com.pzr.xls2jd.initialization;

import com.pzr.xls2jd.proccesor.ProcessorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

/**
 * @Author pzr
 * @date:2022-12-09-11:07
 * @Description:
 **/
@Service
@RequiredArgsConstructor
public class Initializer implements ApplicationRunner {
    private final ProcessorProvider processorProvider;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        processorProvider.initMap(); //初始化processorMap
        System.out.println("初始化processorMap： "+processorProvider.processorMap().size());
    }
}
