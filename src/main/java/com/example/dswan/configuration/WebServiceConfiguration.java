package com.example.dswan.configuration;

import com.example.dswan.services.WebService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;

@Configuration
public class WebServiceConfiguration {
    private final ServletOAuth2AuthorizedClientExchangeFilterFunction apigeeXFilterFunction;
    private final ServletOAuth2AuthorizedClientExchangeFilterFunction apigeeEdgeFilterFunction;
    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager;

    public WebServiceConfiguration(ServletOAuth2AuthorizedClientExchangeFilterFunction apigeeXFilterFunction,
                                   ServletOAuth2AuthorizedClientExchangeFilterFunction apigeeEdgeFilterFunction,
                                   AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        this.apigeeXFilterFunction = apigeeXFilterFunction;
        this.apigeeEdgeFilterFunction = apigeeEdgeFilterFunction;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean("apigeeXWebClientBuilderFactory")
    public WebClientBuilderFactory apigeeXWebClientBuilderFactory() {
        return new WebClientBuilderFactory(authorizedClientManager, apigeeXFilterFunction);
    }

    @Bean("apigeeEdgeWebClientBuilderFactory")
    public WebClientBuilderFactory apigeeEdgeWebClientBuilderFactory() {
        return new WebClientBuilderFactory(authorizedClientManager, apigeeEdgeFilterFunction);
    }

    @Bean("apigeeXWebService")
    public WebService apigeeXWebService(@Qualifier("apigeeXWebClientBuilderFactory") WebClientBuilderFactory webClientBuilderFactory) {
        return new WebService(webClientBuilderFactory);
    }

    @Bean("apigeeEdgeWebService")
    public WebService apigeeEdgeWebService(@Qualifier("apigeeEdgeWebClientBuilderFactory") WebClientBuilderFactory webClientBuilderFactory) {
        return new WebService(webClientBuilderFactory);
    }
}
