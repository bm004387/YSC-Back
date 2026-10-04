package com.buc.ysc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.buc.ysc.**.mapper")
public class YscApplication {

    public static void main(String[] args) {
        SpringApplication.run(YscApplication.class, args);
    }
}