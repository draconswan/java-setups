package com.example.dswan.configuration;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AppConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer externalPropertySourcesPlaceholderConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ServerAddressPropertyEditorRegistrar serverAddressPropertyEditorRegistrar() {
        return new ServerAddressPropertyEditorRegistrar();
    }

    @Bean
    public static CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer configurer = new CustomEditorConfigurer();
        configurer.setPropertyEditorRegistrars(new PropertyEditorRegistrar[]{new ServerAddressPropertyEditorRegistrar()});
        return configurer;
    }

    @Bean
    public Jaxb2RootElementHttpMessageConverter jaxbConverter() {
        return new Jaxb2RootElementHttpMessageConverter();
    }
}
