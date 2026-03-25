package net.restapi.emp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("local")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //.allowedOrigins("*") // 허용할 도메인 설정
                .allowedOriginPatterns("*")
                // OPTIONS는 preflight 요청 처리에 필수
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Authorization: JWT Bearer 토큰 전송에 필수
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
                .allowCredentials(true) // 인증정보 허용 여부
                .maxAge(3600); // preflight 요청의 유효시간(초) — 동일 경로 반복 preflight 감소
    }
}