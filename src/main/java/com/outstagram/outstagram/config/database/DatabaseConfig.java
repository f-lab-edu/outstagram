package com.outstagram.outstagram.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class DatabaseConfig {

    @Bean(name = "shard0DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.shard0")
    public DataSource shard0DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "shard1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.shard1")
    public DataSource shard1DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public DataSourceRouting dataSourceRouting() {
        DataSourceRouting dataSourceRouting = new DataSourceRouting();
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(0, shard0DataSource());
        dataSources.put(1, shard1DataSource());
        dataSourceRouting.setTargetDataSources(dataSources);
        dataSourceRouting.setDefaultTargetDataSource(shard1DataSource());
        return dataSourceRouting;
    }
}
