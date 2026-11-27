package com.autota.server.global.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 응답의 시각을 명세 형식(2026-11-21T23:59:00, 초까지·오프셋 없음)으로 고정한다.
 * 서버가 만든 시각(createdAt 등)에는 초 아래 값이 있어서, 그대로 두면 소수점이 붙어 나간다.
 * 요청 파싱에는 영향이 없다.
 */
@Configuration
public class JacksonConfig {
    private static final DateTimeFormatter RESPONSE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Bean
    public JsonMapperBuilderCustomizer localDateTimeResponseFormat() {
        return builder -> builder.addModule(
                new SimpleModule().addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(RESPONSE_FORMAT)));
    }
}
