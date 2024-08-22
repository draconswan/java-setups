package com.example.dswan.configuration;

import com.example.dswan.converters.MongoDateTimeFromStringConverter;
import com.example.dswan.converters.MongoLocalDateTimeFromStringConverter;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Profile("localdb")
@EnableMongoAuditing
@Configuration
public class MongoConfigConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String databaseURI;

    @Value("${spring.data.mongodb.username}")
    private String mongoUsername;

    @Value("${spring.data.mongodb.password}")
    private String mongoPassword;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authDatabaseName;

    @Value("${spring.data.mongodb.custom.write-concern}")
    private String mongoWriteConcern;

    @Value("${spring.data.mongodb.custom.read-concern}")
    private String mongoReadPreference;

    @Value("${spring.data.mongodb.custom.connection-timeout}")
    private int mongoConnectTimeout;

    @Value("${spring.data.mongodb.custom.read-timeout}")
    private int mongoReadTimeout;

    @Value("${spring.data.mongodb.custom.connections-per-host}")
    private int mongoConnectionsPerHost;


    @Bean(name = "mappingContext")
    public MongoMappingContext mappingContext() {
        return new MongoMappingContext();
    }

    @Bean(name = "mongo")
    public MongoClient mongoClient() {
        MongoClientSettings build = getMongoClientSettings(mongoWriteConcern, mongoReadPreference);
        return MongoClients.create(build);
    }

    @Bean(name = "mongoDbFactory")
    public MongoDatabaseFactory mongoDbFactory() {
        return new SimpleMongoClientDatabaseFactory(mongoClient(), databaseName);
    }

    @Bean(name = "mappingMongoConverter")
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory, MongoCustomConversions customConversions, MongoMappingContext mappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(databaseFactory);
        MappingMongoConverter mappingMongoConverter = new MappingMongoConverter(dbRefResolver, mappingContext);
        mappingMongoConverter.setMapKeyDotReplacement("#");
        mappingMongoConverter.afterPropertiesSet();
        mappingMongoConverter.setCustomConversions(customConversions);
        return mappingMongoConverter;
    }

    @Bean
    @Primary
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new MongoLocalDateTimeFromStringConverter());
        converterList.add(new MongoDateTimeFromStringConverter());
        return new MongoCustomConversions(converterList);
    }

    @Bean(name = "exampleMongoTemplate")
    public MongoTemplate mongoTemplate(MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDbFactory(), mappingMongoConverter);
    }

    private MongoClientSettings getMongoClientSettings(String writeConcern, String readPreference) {
        return MongoClientSettings.builder()
                                  .credential(MongoCredential.createCredential(mongoUsername, authDatabaseName, mongoPassword.toCharArray()))
                                  .applyConnectionString(new ConnectionString(databaseURI))
                                  .applyToSocketSettings(builder -> builder.connectTimeout(mongoConnectTimeout, TimeUnit.MILLISECONDS)
                                                                           .readTimeout(mongoReadTimeout, TimeUnit.MILLISECONDS))
                                  .applyToConnectionPoolSettings(builder -> builder.maxSize(mongoConnectionsPerHost))
                                  .writeConcern(WriteConcern.valueOf(writeConcern.replace("_", "")))
                                  .readPreference(ReadPreference.valueOf(readPreference.replace("_", "")))
                                  .build();
    }

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
}
