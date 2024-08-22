package com.example.dswan.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class SwaggerConfiguration {

    private final Environment environment;

    @Autowired
    public SwaggerConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI api() {
        String env;
        if (environment.getProperty("environment.key") != null) {
            String envKey = environment.getProperty("environment.key");
            if (envKey != null) {
                env = '[' + envKey.toLowerCase() + ']';
            } else {
                env = "[unknown]";
            }
        } else {
            environment.getActiveProfiles();
            env = Arrays.toString(environment.getActiveProfiles());
        }

        return new OpenAPI()
                .info(new Info()
                              .title("Java Service " + env)
                              .version(environment.getProperty("application.version"))
                              .license(new License().name("UNLICENSED"))
                              .description("Java Example Service")
                );
    }

}
