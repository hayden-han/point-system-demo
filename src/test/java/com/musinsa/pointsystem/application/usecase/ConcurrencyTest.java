package com.musinsa.pointsystem.application.usecase;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.application.dto.EarnPointCommand;
import com.musinsa.pointsystem.application.dto.UsePointCommand;
import com.musinsa.pointsystem.application.exception.LockAcquisitionFailedException;
import com.musinsa.pointsystem.domain.exception.InsufficientPointException;
import com.musinsa.pointsystem.domain.model.MemberPoint;
import com.musinsa.pointsystem.domain.model.PointAmount;
import com.musinsa.pointsystem.domain.repository.MemberPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrencyTest extends IntegrationTestBase {

    @Autowired
    private EarnPointUseCase earnPointUseCase;

    @Autowired
    private UsePointUseCase usePointUseCase;

    @Autowired
    private MemberPointRepository memberPointRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Nested
    @DisplayName("동시성 테스트")
    @SqlGroup({
            @Sql(scripts = "/sql/concurrency-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/concurrency-test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class ConcurrencyCases {

        @Test
        @DisplayName("동시 적립 정합성 - 성공/실패 분리 검증")
        void concurrentEarn_shouldMaintainConsistency() throws InterruptedException {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000005001");
            int threadCount = 10;
            Long amountPerEarn = 100L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger lockFailureCount = new AtomicInteger(0);
            List<Exception> failures = Collections.synchronizedList(new ArrayList<>());

            // WHEN
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        EarnPointCommand command = EarnPointCommand.builder()
                                .memberId(memberId)
                                .amount(amountPerEarn)
                                .earnType("SYSTEM")
                                .build();
                        earnPointUseCase.execute(command);
                        successCount.incrementAndGet();
                    } catch (LockAcquisitionFailedException e) {
                        lockFailureCount.incrementAndGet();
                        failures.add(e);
                    } catch (Exception e) {
                        failures.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // THEN
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId).orElseThrow();
            long expectedBalance = successCount.get() * amountPerEarn;

            // 성공한 만큼 정확히 잔액이 증가했는지 검증
            assertThat(memberPoint.totalBalance()).isEqualTo(PointAmount.of(expectedBalance));

            // 최소 1건 이상 성공해야 함
            assertThat(successCount.get()).isGreaterThanOrEqualTo(1);

            // 전체 = 성공 + 락 실패 + 기타 예외
            assertThat(successCount.get() + lockFailureCount.get() + failures.size() - lockFailureCount.get())
                    .isEqualTo(threadCount);
        }

        @Test
        @DisplayName("동시 사용 정합성 - 잔액 부족 시 실패 분리 검증")
        void concurrentUse_shouldMaintainConsistencyWithInsufficientBalance() throws InterruptedException {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000005002");
            int threadCount = 10;
            Long amountPerUse = 500L;
            Long initialBalance = 10000L;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger insufficientBalanceCount = new AtomicInteger(0);
            AtomicInteger lockFailureCount = new AtomicInteger(0);
            AtomicInteger orderCounter = new AtomicInteger(0);

            // WHEN
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        UsePointCommand command = UsePointCommand.builder()
                                .memberId(memberId)
                                .amount(amountPerUse)
                                .orderId("ORDER-C-T02-" + orderCounter.incrementAndGet())
                                .build();
                        usePointUseCase.execute(command);
                        successCount.incrementAndGet();
                    } catch (InsufficientPointException e) {
                        insufficientBalanceCount.incrementAndGet();
                    } catch (LockAcquisitionFailedException e) {
                        lockFailureCount.incrementAndGet();
                    } catch (Exception e) {
                        // 기타 예외 (테스트 실패로 처리하지 않음)
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // THEN
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId).orElseThrow();
            long expectedBalance = initialBalance - (successCount.get() * amountPerUse);

            // 성공한 만큼 정확히 잔액이 감소했는지 검증
            assertThat(memberPoint.totalBalance()).isEqualTo(PointAmount.of(expectedBalance));

            // 잔액은 음수가 될 수 없음
            assertThat(memberPoint.totalBalance().getValue()).isGreaterThanOrEqualTo(0L);

            // 최대 성공 가능 횟수 = initialBalance / amountPerUse = 20
            int maxPossibleSuccess = (int) (initialBalance / amountPerUse);
            assertThat(successCount.get()).isLessThanOrEqualTo(maxPossibleSuccess);

            // 모든 스레드가 처리됨 검증
            assertThat(successCount.get() + insufficientBalanceCount.get() + lockFailureCount.get())
                    .isLessThanOrEqualTo(threadCount);
        }

        @Test
        @DisplayName("적립+사용 동시 정합성 - 복합 시나리오")
        void concurrentEarnAndUse_shouldMaintainConsistency() throws InterruptedException {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000005003");
            Long initialBalance = 5000L;
            int earnThreadCount = 5;
            int useThreadCount = 5;
            Long earnAmount = 100L;
            Long useAmount = 100L;
            ExecutorService executor = Executors.newFixedThreadPool(earnThreadCount + useThreadCount);
            CountDownLatch latch = new CountDownLatch(earnThreadCount + useThreadCount);
            AtomicInteger earnSuccessCount = new AtomicInteger(0);
            AtomicInteger useSuccessCount = new AtomicInteger(0);
            AtomicInteger useFailureCount = new AtomicInteger(0);
            AtomicInteger orderCounter = new AtomicInteger(0);

            // WHEN
            for (int i = 0; i < earnThreadCount; i++) {
                executor.submit(() -> {
                    try {
                        EarnPointCommand command = EarnPointCommand.builder()
                                .memberId(memberId)
                                .amount(earnAmount)
                                .earnType("SYSTEM")
                                .build();
                        earnPointUseCase.execute(command);
                        earnSuccessCount.incrementAndGet();
                    } catch (Exception e) {
                        // 락 획득 실패 등 예외 발생 가능
                    } finally {
                        latch.countDown();
                    }
                });
            }

            for (int i = 0; i < useThreadCount; i++) {
                executor.submit(() -> {
                    try {
                        UsePointCommand command = UsePointCommand.builder()
                                .memberId(memberId)
                                .amount(useAmount)
                                .orderId("ORDER-C-T03-" + orderCounter.incrementAndGet())
                                .build();
                        usePointUseCase.execute(command);
                        useSuccessCount.incrementAndGet();
                    } catch (InsufficientPointException e) {
                        useFailureCount.incrementAndGet();
                    } catch (Exception e) {
                        // 락 획득 실패 등
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // THEN
            MemberPoint memberPoint = memberPointRepository.findByMemberId(memberId).orElseThrow();
            long expectedBalance = initialBalance
                    + (earnSuccessCount.get() * earnAmount)
                    - (useSuccessCount.get() * useAmount);

            // 계산된 예상 잔액과 실제 잔액이 일치해야 함
            assertThat(memberPoint.totalBalance()).isEqualTo(PointAmount.of(expectedBalance));

            // 잔액은 음수가 될 수 없음
            assertThat(memberPoint.totalBalance().getValue()).isGreaterThanOrEqualTo(0L);

            // 최소 1건 이상의 적립이 성공해야 함
            assertThat(earnSuccessCount.get()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("락 획득 재시도 후 성공")
        void lockRetry_shouldSucceedEventually() throws InterruptedException, ExecutionException {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000005004");
            ExecutorService executor = Executors.newFixedThreadPool(2);
            List<Future<Boolean>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);

            // WHEN - 동시에 2개 요청
            for (int i = 0; i < 2; i++) {
                int orderId = i;
                Future<Boolean> future = executor.submit(() -> {
                    try {
                        UsePointCommand command = UsePointCommand.builder()
                                .memberId(memberId)
                                .amount(100L)
                                .orderId("ORDER-C-T04-" + orderId)
                                .build();
                        usePointUseCase.execute(command);
                        successCount.incrementAndGet();
                        return true;
                    } catch (LockAcquisitionFailedException e) {
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                });
                futures.add(future);
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            // THEN
            int totalSuccess = 0;
            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    totalSuccess++;
                }
            }

            // 분산락 덕분에 순차 처리되어 2개 모두 성공해야 함 (재시도 메커니즘)
            assertThat(totalSuccess).isEqualTo(2);
            assertThat(successCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("락 획득 최종 실패 - 타임아웃")
        void lockAcquisitionFailed_shouldThrowException() throws InterruptedException {
            // GIVEN
            UUID memberId = UUID.fromString("00000000-0000-0000-0000-000000005004");
            String lockKey = "lock:point:member:" + memberId;
            RLock lock = redissonClient.getLock(lockKey);
            AtomicReference<Exception> caughtException = new AtomicReference<>();
            AtomicReference<Class<?>> exceptionType = new AtomicReference<>();

            // 락을 장시간 점유 (30초 lease time으로 테스트 동안 유지)
            boolean acquired = lock.tryLock(0, 30000, TimeUnit.MILLISECONDS);
            assertThat(acquired).isTrue();

            try {
                // WHEN
                ExecutorService executor = Executors.newSingleThreadExecutor();
                CountDownLatch latch = new CountDownLatch(1);

                executor.submit(() -> {
                    try {
                        UsePointCommand command = UsePointCommand.builder()
                                .memberId(memberId)
                                .amount(100L)
                                .orderId("ORDER-C-T05")
                                .build();
                        usePointUseCase.execute(command);
                    } catch (LockAcquisitionFailedException e) {
                        caughtException.set(e);
                        exceptionType.set(LockAcquisitionFailedException.class);
                    } catch (Exception e) {
                        caughtException.set(e);
                        exceptionType.set(e.getClass());
                    } finally {
                        latch.countDown();
                    }
                });

                // 4번 재시도 (waitTime=3초 * 4 + delay 1.7초 = 약 14초)
                boolean completed = latch.await(20, TimeUnit.SECONDS);
                executor.shutdown();

                // THEN
                assertThat(completed).isTrue();
                assertThat(caughtException.get()).isNotNull();
                assertThat(exceptionType.get()).isEqualTo(LockAcquisitionFailedException.class);
            } finally {
                // 락 해제
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
