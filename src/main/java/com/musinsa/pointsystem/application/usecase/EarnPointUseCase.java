package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.EarnPointResult;
import com.musinsa.pointsystem.application.port.DistributedLock;
import com.musinsa.pointsystem.common.time.TimeProvider;
import com.musinsa.pointsystem.domain.model.EarnPolicyConfig;
import com.musinsa.pointsystem.domain.model.EarnType;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.model.PointLedger;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.service.PointAccrualManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EarnPointUseCase {

    private final MemberPointRepository memberPointRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final PointAccrualManager pointAccrualManager;
    private final TimeProvider timeProvider;

    @DistributedLock(key = "'lock:point:member:' + #command.memberId")
    @Transactional
    public EarnPointResult execute(EarnPointCommand command) {
        log.info("포인트 적립 시작. memberId={}, amount={}, earnType={}",
                command.memberId(), command.amount(), command.earnType());

        LocalDateTime now = timeProvider.now();

        // DTO → 도메인 타입 변환
        PointAmount amount = PointAmount.of(command.amount());
        EarnType earnType = EarnType.valueOf(command.earnType());

        // 정책 조회
        EarnPolicyConfig policy = pointPolicyRepository.getEarnPolicyConfig();

        // 회원 포인트 조회 (Entry 포함)
        MemberPoint memberPoint = memberPointRepository.getOrCreateWithAllLedgersAndEntries(command.memberId());

        // Domain Service를 통한 적립 처리 (EARN Entry 자동 생성)
        MemberPoint.EarnResult earnResult = pointAccrualManager.earnWithExpirationValidationV2(
                memberPoint,
                amount,
                earnType,
                command.expirationDays(),
                policy
        );

        // 결과에서 새 객체 추출
        MemberPoint updatedMemberPoint = earnResult.memberPoint();
        PointLedger ledger = earnResult.ledger();

        // MemberPoint + Ledgers + Entries 저장
        memberPointRepository.saveWithEntries(updatedMemberPoint);

        log.info("포인트 적립 완료. memberId={}, ledgerId={}, earnedAmount={}, totalBalance={}",
                command.memberId(), ledger.id(), amount.getValue(),
                updatedMemberPoint.getTotalBalance(now).getValue());

        return EarnPointResult.builder()
                .ledgerId(ledger.id())
                .memberId(command.memberId())
                .earnedAmount(amount.getValue())
                .totalBalance(updatedMemberPoint.getTotalBalance(now).getValue())
                .expiredAt(ledger.expiredAt())
                .build();
    }
}
