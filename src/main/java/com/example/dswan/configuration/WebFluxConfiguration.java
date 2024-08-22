package com.example.dswan.configuration;

import com.example.dswan.util.LoggingContextFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import jakarta.servlet.Filter;

@Configuration
public class WebFluxConfiguration implements WebFluxConfigurer {

    @Bean
    @Primary
    public ObjectMapper jacksonMapper() {
        ObjectMapper customObjectMapper = new ObjectMapper();
        customObjectMapper.registerModule(new JodaModule());
        customObjectMapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                                                                                   new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));
        customObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return customObjectMapper;
    }

    @Bean
    public Filter loggingFilter() {
        return new LoggingContextFilter();
    }

    @Override
    public void configureContentTypeResolver(RequestedContentTypeResolverBuilder builder) {
        builder.fixedResolver(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
        builder.parameterResolver()
               .mediaType("xml", MediaType.APPLICATION_XML)
               .mediaType("json", MediaType.APPLICATION_JSON);
        WebFluxConfigurer.super.configureContentTypeResolver(builder);
    }
}
