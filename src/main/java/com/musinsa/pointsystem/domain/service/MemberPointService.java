package com.musinsa.pointsystem.domain.service;

import com.musinsa.pointsystem.domain.exception.MemberPointNotFoundException;
import com.musinsa.pointsystem.domain.exception.PointTransactionNotFoundException;
import com.musinsa.pointsystem.domain.model.*;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import com.musinsa.pointsystem.domain.repository.PointPolicyRepository;
import com.musinsa.pointsystem.domain.repository.PointTransactionRepository;
import com.musinsa.pointsystem.domain.repository.PointUsageDetailRepository;

import java.util.List;
import java.util.UUID;

/**
 * 회원 포인트 도메인 서비스
 *
 * <p>순수 POJO로 구현되어 Spring 의존성이 없습니다.
 * Infrastructure 레이어의 DomainServiceConfig에서 Bean으로 등록됩니다.</p>
 *
 * <p>Repository를 통한 조회와 도메인 예외 발생을 담당하여
 * UseCase에서 반복되는 조회+예외 처리 로직을 캡슐화합니다.</p>
 */
public class MemberPointService {

    private final MemberPointRepository memberPointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointUsageDetailRepository pointUsageDetailRepository;
    private final PointPolicyRepository pointPolicyRepository;

    public MemberPointService(
            MemberPointRepository memberPointRepository,
            PointTransactionRepository pointTransactionRepository,
            PointUsageDetailRepository pointUsageDetailRepository,
            PointPolicyRepository pointPolicyRepository
    ) {
        this.memberPointRepository = memberPointRepository;
        this.pointTransactionRepository = pointTransactionRepository;
        this.pointUsageDetailRepository = pointUsageDetailRepository;
        this.pointPolicyRepository = pointPolicyRepository;
    }

    // =====================================================
    // 조회 메서드 (비즈니스 규칙에 따른 예외 발생)
    // =====================================================

    /**
     * 회원 포인트를 Ledgers와 함께 조회
     * @throws MemberPointNotFoundException 회원을 찾을 수 없는 경우
     */
    public MemberPoint getMemberPointWithLedgers(UUID memberId) {
        return memberPointRepository.findByMemberIdWithLedgers(memberId)
                .orElseThrow(() -> new MemberPointNotFoundException(memberId));
    }

    /**
     * 회원 포인트를 Ledgers와 함께 조회, 없으면 생성
     */
    public MemberPoint getOrCreateMemberPointWithLedgers(UUID memberId) {
        return memberPointRepository.getOrCreateWithLedgers(memberId);
    }

    /**
     * 회원 포인트를 사용 가능한 Ledgers와 함께 조회, 없으면 생성
     */
    public MemberPoint getOrCreateMemberPointWithAvailableLedgers(UUID memberId) {
        return memberPointRepository.getOrCreateWithAvailableLedgers(memberId);
    }

    /**
     * 트랜잭션 조회
     * @throws PointTransactionNotFoundException 트랜잭션을 찾을 수 없는 경우
     */
    public PointTransaction getTransaction(UUID transactionId) {
        return pointTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new PointTransactionNotFoundException(transactionId));
    }

    /**
     * 취소 가능한 사용 상세 조회 (만료일 긴 것부터)
     */
    public List<PointUsageDetail> getCancelableUsageDetails(UUID transactionId) {
        return pointUsageDetailRepository.findCancelableByTransactionId(transactionId);
    }

    // =====================================================
    // 정책 조회
    // =====================================================

    /**
     * 적립 정책 조회
     */
    public EarnPolicyConfig getEarnPolicy() {
        return pointPolicyRepository.getEarnPolicyConfig();
    }

    /**
     * 만료 정책 조회
     */
    public ExpirationPolicyConfig getExpirationPolicy() {
        return pointPolicyRepository.getExpirationPolicyConfig();
    }

    // =====================================================
    // 저장 메서드
    // =====================================================

    /**
     * MemberPoint와 Ledgers 함께 저장
     */
    public MemberPoint saveMemberPoint(MemberPoint memberPoint) {
        return memberPointRepository.save(memberPoint);
    }

    /**
     * 트랜잭션 저장
     */
    public PointTransaction saveTransaction(PointTransaction transaction) {
        return pointTransactionRepository.save(transaction);
    }

    /**
     * 사용 상세 일괄 저장
     */
    public void saveUsageDetails(List<PointUsageDetail> usageDetails) {
        pointUsageDetailRepository.saveAll(usageDetails);
    }
}
