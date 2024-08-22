package com.example.dswan.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CustomHeader {
    public static final String REQUEST_ID = "X-Request-ID";

    public static Map<String, String> injectTraceHeaders() {
        String requestId = null;

        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest servletRequest;
            servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            requestId = servletRequest.getHeader(CustomHeader.REQUEST_ID);
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put(CustomHeader.REQUEST_ID, Objects.requireNonNullElse(requestId, UUID.randomUUID().toString()));
        return headers;
    }
}
