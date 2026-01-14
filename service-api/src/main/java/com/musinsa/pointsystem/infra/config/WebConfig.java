package com.musinsa.pointsystem.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * - CORS 정책: 프로덕션 환경에서는 허용 도메인을 제한 필요
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "https://www.musinsa.com",
                        "https://musinsa.com",
                        "https://admin.musinsa.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // preflight 캐시 1시간

        // Admin API는 내부망만 허용 (추후 IP 제한 추가)
        registry.addMapping("/admin/**")
                .allowedOrigins("https://admin.musinsa.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
