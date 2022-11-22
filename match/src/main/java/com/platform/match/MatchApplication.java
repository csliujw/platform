package com.platform.match;

import com.platform.match.service.MatchingServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class MatchApplication {

    public static void main(String[] args) {
        // 使用线程池确保不会因为某些异常挂掉后无法执行后面的任务。
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(MatchingServiceImpl.matchingPool::execute);
        SpringApplication.run(MatchApplication.class, args);
    }
}
