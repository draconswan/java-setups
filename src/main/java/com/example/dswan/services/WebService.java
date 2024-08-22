package com.example.dswan.services;

import com.example.dswan.configuration.WebClientBuilderFactory;
import com.example.dswan.util.CustomHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class WebService {
    final WebClientBuilderFactory webClientBuilderFactory;

    @Value("${webservice.connection.timeout}")
    int timeout;

    @Value("${webservice.environment}")
    String activeProfile;

    @Value("${app.version:unknown}")
    String clientVersion;

    public WebService(WebClientBuilderFactory webClientBuilderFactory) {
        this.webClientBuilderFactory = webClientBuilderFactory;
    }

    public Builder get(String url) {
        return new Builder(webClientBuilderFactory.getObject(), timeout, url, HttpMethod.GET, null, activeProfile, clientVersion);
    }

    public Builder put(String url) {
        return put(url, null);
    }

    public Builder put(String url, Object body) {
        return new Builder(webClientBuilderFactory.getObject(), timeout, url, HttpMethod.PUT, body, activeProfile, clientVersion);
    }

    public Builder post(String url) {
        return post(url, null);
    }

    public Builder post(String url, Object body) {
        return new Builder(webClientBuilderFactory.getObject(), timeout, url, HttpMethod.POST, body, activeProfile, clientVersion);
    }

    public Builder delete(String url) {
        return new Builder(webClientBuilderFactory.getObject(), timeout, url, HttpMethod.DELETE, null, activeProfile, clientVersion);
    }

    public static class Builder {
        private final WebClient.Builder webClientBuilder;
        private int timeout;
        private final HttpMethod method;
        private final String url;
        private final String profile;
        private final String clientVersion;
        private final MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        private Object body;
        private final HttpHeaders headers = new HttpHeaders();
        private Function<ClientResponse, Mono<? extends Throwable>> exceptionFunction;
        private final HashMap<String, Integer> taggedFilters = new HashMap<>();

        private Builder(WebClient.Builder webClientBuilder, int timeout, String url, HttpMethod method, Object body, String profile, String clientVersion) {
            this.timeout = timeout;
            this.url = url;
            this.method = method;
            this.body = body;
            this.profile = profile;
            this.clientVersion = clientVersion;
            this.webClientBuilder = webClientBuilder;
        }

        public Builder setBody(Object body) {
            this.body = body;
            return this;
        }

        public Builder setExceptionFunction(Function<ClientResponse, Mono<? extends Throwable>> exceptionFunction) {
            this.exceptionFunction = exceptionFunction;
            return this;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder addHeaders(HttpHeaders headers) {
            this.headers.addAll(headers);
            return this;
        }

        public Builder addParam(String key, Object value) {
            this.params.add(key, value);
            return this;
        }

        public Builder addParams(Map<String, Object> params) {
            params.forEach(this.params::add);
            return this;
        }

        public Builder addParams(MultiValueMap<String, Object> params) {
            this.params.addAll(params);
            return this;
        }

        public Builder codecs(Consumer<ClientCodecConfigurer> configurer) {
            webClientBuilder.codecs(configurer);
            return this;
        }

        public String execute() {
            return execute(String.class);
        }

        public <T> T execute(Class<T> responseClass) {
            WebClient.ResponseSpec client = clientRequest();
            return requestHelper(client, responseClass, exceptionFunction).getBody();
        }

        public <T> ResponseEntity<T> executeEntity(Class<T> responseClass) {
            WebClient.ResponseSpec client = clientRequest();
            return requestHelper(client, responseClass, exceptionFunction);
        }

        public <T> T execute(ParameterizedTypeReference<T> responseClassRef) {
            WebClient.ResponseSpec client = clientRequest();
            return requestHelper(client, responseClassRef, exceptionFunction).getBody();
        }

        private Consumer<HttpHeaders> mergeHeaders() {
            final HttpHeaders providedHeaders = this.headers;
            if (!providedHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                providedHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
            return mergedHeaders -> providedHeaders
                    .keySet()
                    .forEach(
                            key -> Objects.requireNonNull(providedHeaders.get(key)).forEach(nested -> mergedHeaders.add(key, nested))
                    );
        }

        private UriComponentsBuilder generateURIBuilderWithParams() {
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder
                        .fromUriString(url);
                params.forEach(uriBuilder::queryParam);
                return uriBuilder;
            } catch (Exception ex) {
                return null;
            }
        }

        private WebClient.ResponseSpec clientRequest() {
            URI uri = generateURIBuilderWithParams().build(true).toUri();
            headers.setAll(CustomHeader.injectTraceHeaders());

            WebClient.RequestBodySpec bodySpec = webClientBuilder.build()
                                                                 .method(method)
                                                                 .uri(uri)
                                                                 .headers(mergeHeaders());
            if (body == null) {
                return bodySpec.accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                               .retrieve();
            } else {
                return bodySpec.bodyValue(body)
                               .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML)
                               .retrieve();
            }
        }

        private <T> ResponseEntity<T> requestHelper(WebClient.ResponseSpec webclientRetrieve,
                                                    ParameterizedTypeReference<T> responseRefClass, Function<ClientResponse, Mono<? extends Throwable>> exceptionFunction) {
            Mono<ResponseEntity<T>> response = webclientRetrieve
                    .onStatus(
                            httpStatus -> !httpStatus.is2xxSuccessful() && !httpStatus.is1xxInformational(),
                            exceptionFunction != null ? exceptionFunction : ClientResponse::createException)
                    .toEntity(responseRefClass)
                    .timeout(Duration.ofSeconds(timeout))
                    .doOnError(error -> {
                        if (error instanceof TimeoutException) {
                            log.warn("WebService timeout exception occurred: ", error);
                        }
                    })
                    .retry(1)
                    .share();
            return response.block();
        }

        private <T> ResponseEntity<T> requestHelper(WebClient.ResponseSpec webclientRetrieve, Class<T> responseClass,
                                                    Function<ClientResponse, Mono<? extends Throwable>> exceptionFunction) {
            Mono<ResponseEntity<T>> response = webclientRetrieve
                    .onStatus(
                            httpStatus -> !httpStatus.is2xxSuccessful() && !httpStatus.is1xxInformational(),
                            exceptionFunction != null ? exceptionFunction : ClientResponse::createException)
                    .toEntity(responseClass)
                    .timeout(Duration.ofSeconds(timeout))
                    .doOnError(error -> {
                        if (error instanceof TimeoutException) {
                            log.warn("WebService timeout exception occurred: ", error);
                        }
                    })
                    .retry(1)
                    .share();
            return response.block();
        }
    }
}
