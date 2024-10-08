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
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메소드 설정
                .allowedHeaders("Origin", "Content-Type", "Accept") // 허용할 헤더 설정
                .allowCredentials(true) // 인증정보 허용 여부
                .maxAge(3600); // preflight 요청의 유효시간 설정
    }
}