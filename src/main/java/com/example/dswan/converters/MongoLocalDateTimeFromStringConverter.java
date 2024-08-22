package com.example.dswan.converters;

import org.joda.time.LocalDateTime;
import org.springframework.core.convert.converter.Converter;

public final class MongoLocalDateTimeFromStringConverter implements Converter<String, LocalDateTime> {
    @Override
    public LocalDateTime convert(String source) {
        return source == null ? null : LocalDateTime.parse(source);
    }
}
