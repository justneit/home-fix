package com.homefix.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/** One MySQL 8.4 container shared by every Spring test context in the JVM. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("homefix")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");

    @Bean(destroyMethod = "")
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return MYSQL;
    }
}
