package com.example.dswan.configuration;

import com.example.dswan.services.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class WebClientBuilderFactory implements FactoryBean<WebClient.Builder> {

    AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager;
    ServletOAuth2AuthorizedClientExchangeFilterFunction filterFunction;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientBuilderFactory.class);

    public WebClientBuilderFactory(AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager,
                                   ServletOAuth2AuthorizedClientExchangeFilterFunction filterFunction) {
        this.clientManager = clientManager;
        this.filterFunction = filterFunction;
    }

    @Override
    public WebClient.Builder getObject() {
        return WebClient.builder()
                        .filter(filterFunction)
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .filter(logRequestFilter());
    }

    @Override
    public Class<?> getObjectType() {
        return WebService.class;
    }

    private ExchangeFilterFunction logRequestFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Request Headers: [\n");
                clientRequest.headers()
                             .forEach((name, values) -> values.forEach(value -> sb.append("\t").append(name).append(": ").append(value).append(";\n")));
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n]");
                LOGGER.debug("Sending request to: " + clientRequest.url());
                LOGGER.debug(sb.toString());
            }
            return Mono.just(clientRequest);
        });
    }
}
