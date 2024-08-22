package com.example.dswan.configuration;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "caching")
@EnableCaching
@PropertySource("classpath:git.properties")
public class CacheConfig {

    @Value("${git.commit.id.abbrev}")
    private String commitSha;
    @Value("${spring.cache.redis.time-to-live}")
    private Long defaultTTL;
    @Setter
    private Map<String, CacheSpec> specs;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    @Primary
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .entryTtl(Duration.ofSeconds(defaultTTL)).prefixCacheNameWith(commitSha + "-");
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration cacheConfiguration) {
        return builder -> {
            specs.forEach((cacheName, cacheSpec) -> {
                              LOGGER.debug("Setting Cache TTL for {} expiration: {}", cacheName, cacheSpec.getExpireTime());
                              builder.withCacheConfiguration(
                                      cacheName,
                                      cacheConfiguration.entryTtl(Duration.ofMinutes(cacheSpec.getExpireTime()))
                              );
                          }
            );
            builder.cacheDefaults(cacheConfiguration);
        };
    }
}
