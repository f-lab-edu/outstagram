package com.outstagram.outstagram.config;

import com.outstagram.outstagram.util.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Bean
    public Snowflake snowflake0() {
        return Snowflake.getInstance(0);
    }

    @Bean
    public Snowflake snowflake1() {
        return Snowflake.getInstance(1);
    }

}
