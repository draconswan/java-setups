package com.example.dswan.converters;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;

public final class MongoDateTimeFromStringConverter implements Converter<String, DateTime> {
    @Override
    public DateTime convert(String source) {
        return source == null ? null : DateTime.parse(source);
    }
}
