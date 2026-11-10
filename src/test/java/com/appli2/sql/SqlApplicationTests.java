package com.appli2.sql;

import com.appli2.accessingdatamysql.AccessingDataMysqlApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = AccessingDataMysqlApplication.class)
class SqlApplicationTests {

    @Test
    void contextLoads() {
    }
}