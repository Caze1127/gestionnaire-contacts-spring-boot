package com.appli2.sql;

import com.appli2.accessingdatamysql.AccessingDataMysqlApplication;
import org.springframework.boot.SpringApplication;

public class TestSqlApplication {

    public static void main(String[] args) {
        SpringApplication
                .from(AccessingDataMysqlApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}