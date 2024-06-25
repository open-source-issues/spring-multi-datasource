package com.foo.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
class DataSourceConfiguration {

    @Bean
    public DataSource writerDataSource(WriterDatasourceProperties writerDatasourceProperties) {
        return new HikariDataSource(writerDatasourceProperties);
    }

    @Bean
    public DataSource readerDataSource(ReaderDatasourceProperties readerDatasourceProperties) {
        return new HikariDataSource(readerDatasourceProperties);
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSource writerDataSource, DataSource readerDataSource) {
        var routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();

        targetDataSources.put(DataSourceType.READ_WRITE, writerDataSource);
        targetDataSources.put(DataSourceType.READ_ONLY, readerDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writerDataSource);

        return routingDataSource;
    }

    @Bean
    public HealthIndicator writerDbHealthIndicator(DataSource writerDataSource) {
        return new DataSourceHealthIndicator(writerDataSource);
    }

    @Bean
    public HealthIndicator readerDbHealthIndicator(DataSource readerDataSource) {
        return new DataSourceHealthIndicator(readerDataSource);
    }
}
