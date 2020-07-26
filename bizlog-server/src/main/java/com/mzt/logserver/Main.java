package com.mzt.logserver;

import com.mzt.logapi.server.annotation.EnableLogRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan("com.mzt")
@EnableTransactionManagement
@EnableLogRecord(bizLine = "com.mzt.test")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
