package com.example.dswan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jsonb.JsonbAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(
        exclude = {
                PropertyPlaceholderAutoConfiguration.class,
                JdbcTemplateAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                ReactiveSecurityAutoConfiguration.class,
        }
)
@Slf4j
public class JavaSetupApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(JavaSetupApplication.class, args);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
