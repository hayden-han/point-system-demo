package com.musinsa.pointsystem.infra.config;

import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;
import com.musinsa.pointsystem.domain.service.MemberPointService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 도메인 서비스 Bean 설정
 *
 * <p>순수 POJO인 도메인 서비스들을 Spring Bean으로 등록합니다.
 * 이를 통해 도메인 레이어는 Spring 의존성 없이 순수하게 유지되면서도
 * Repository 의존성 주입의 이점을 누릴 수 있습니다.</p>
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public MemberPointService memberPointService(
            MemberPointRepository memberPointRepository,
            PointTransactionRepository pointTransactionRepository,
            PointUsageDetailRepository pointUsageDetailRepository,
            PointPolicyRepository pointPolicyRepository
    ) {
        return new MemberPointService(
                memberPointRepository,
                pointTransactionRepository,
                pointUsageDetailRepository,
                pointPolicyRepository
        );
    }
}
