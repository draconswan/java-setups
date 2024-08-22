package com.example.dswan.configuration;

import com.example.dswan.converters.MongoDateTimeFromStringConverter;
import com.example.dswan.converters.MongoLocalDateTimeFromStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("!localdb")
public class CommonMongoConfiguration {
    @Bean
    @Primary
    public MappingMongoConverter mongoConverter(
            @Autowired MongoMappingContext mongoMappingContext,
            @Autowired MongoDatabaseFactory mainMongoFactory,
            @Autowired MongoCustomConversions conversions
    ) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mainMongoFactory);
        MappingMongoConverter mongoConverter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        mongoConverter.setMapKeyDotReplacement("#");
        mongoConverter.afterPropertiesSet();
        mongoConverter.setCustomConversions(conversions);
        return mongoConverter;
    }

    @Bean
    @Primary
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new MongoLocalDateTimeFromStringConverter());
        converterList.add(new MongoDateTimeFromStringConverter());
        return new MongoCustomConversions(converterList);
    }
}
