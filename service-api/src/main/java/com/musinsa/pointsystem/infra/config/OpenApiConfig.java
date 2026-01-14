package com.musinsa.pointsystem.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Local Server")
                ))
                .tags(List.of(
                        new Tag().name("Point Earn").description("포인트 적립 API"),
                        new Tag().name("Point Use").description("포인트 사용 API"),
                        new Tag().name("Point Balance").description("포인트 잔액/이력 조회 API")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("Musinsa Point System API")
                .description("무신사 포인트 시스템 REST API 문서\n\n" +
                        "## 주요 기능\n" +
                        "- **적립**: 포인트 적립 및 적립 취소\n" +
                        "- **사용**: 포인트 사용 및 사용 취소\n" +
                        "- **조회**: 잔액 조회 및 거래 이력 조회\n\n" +
                        "## 포인트 정책\n" +
                        "- 1회 적립 최소/최대 금액: 1원 ~ 1,000,000원\n" +
                        "- 개인별 최대 보유 금액: 10,000,000원\n" +
                        "- 만료일: 기본 365일 (최소 1일 ~ 최대 5년)\n" +
                        "- 사용 우선순위: 수기 지급 → 만료일 짧은 순")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Musinsa Point Team")
                        .email("point@musinsa.com"));
    }
}
