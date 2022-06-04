package com.gaspar.logprocessor.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .callTimeout(Duration.of(10, ChronoUnit.MINUTES))
                .connectTimeout(Duration.of(10, ChronoUnit.MINUTES))
                .readTimeout(Duration.of(10, ChronoUnit.MINUTES))
                .build();
    }

}
