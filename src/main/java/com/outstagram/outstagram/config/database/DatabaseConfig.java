package com.outstagram.outstagram.config.database;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Primary
    @Bean(name = "shard0")
    @ConfigurationProperties(prefix = "spring.datasource.shard0")
    public DataSource shard0DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "shard1")
    @ConfigurationProperties(prefix = "spring.datasource.shard1")
    public DataSource shard1DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource routingDataSource(
        @Qualifier("shard0") DataSource shard0DataSource,
        @Qualifier("shard1") DataSource shard1DataSource) {

        DataSourceRouting dynamicDataSource = new DataSourceRouting();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(0L, shard0DataSource);
        dataSourceMap.put(1L, shard1DataSource);

        dynamicDataSource.setDefaultTargetDataSource(shard1DataSource);
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        return dynamicDataSource;
    }
}
