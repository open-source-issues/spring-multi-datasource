package com.foo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource.writer")
class WriterDatasourceProperties extends HikariConfig {
}
