package com.autota.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        // 시각은 요청·응답 모두 Asia/Seoul 기준(오프셋 없음)이라 서버 타임존을 고정한다.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(ServerApplication.class, args);
    }

}
