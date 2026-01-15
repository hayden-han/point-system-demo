package com.musinsa.pointsystem.infra.idempotency;

import com.musinsa.pointsystem.IntegrationTestBase;
import com.musinsa.pointsystem.domain.infrastructure.IdempotencyKeyPort.AcquireResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeyRepositoryTest extends IntegrationTestBase {

    @Autowired
    private IdempotencyKeyRepository repository;

    private String testKey;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 고유한 키 사용
        testKey = "test-" + UUID.randomUUID();
    }

    @Nested
    @DisplayName("tryAcquire")
    class TryAcquireTest {

        @Test
        @DisplayName("첫 번째 요청은 ACQUIRED 반환")
        void firstRequest_shouldReturnAcquired() {
            // WHEN
            AcquireResult result = repository.tryAcquire(testKey);

            // THEN
            assertThat(result).isEqualTo(AcquireResult.ACQUIRED);
        }

        @Test
        @DisplayName("처리 중인 키에 대한 두 번째 요청은 PROCESSING 반환")
        void secondRequestWhileProcessing_shouldReturnProcessing() {
            // GIVEN - 첫 번째 요청이 처리 중
            repository.tryAcquire(testKey);

            // WHEN - 두 번째 요청
            AcquireResult result = repository.tryAcquire(testKey);

            // THEN
            assertThat(result).isEqualTo(AcquireResult.PROCESSING);
        }

        @Test
        @DisplayName("처리 완료된 키에 대한 요청은 ALREADY_COMPLETED 반환")
        void requestAfterCompletion_shouldReturnAlreadyCompleted() {
            // GIVEN - 처리 완료
            repository.tryAcquire(testKey);
            repository.saveResult(testKey, "transaction-123");

            // WHEN
            AcquireResult result = repository.tryAcquire(testKey);

            // THEN
            assertThat(result).isEqualTo(AcquireResult.ALREADY_COMPLETED);
        }
    }

    @Nested
    @DisplayName("saveResult")
    class SaveResultTest {

        @Test
        @DisplayName("결과 저장 후 조회 가능")
        void saveResult_shouldBeRetrievable() {
            // GIVEN
            repository.tryAcquire(testKey);
            String expectedResult = "transaction-" + UUID.randomUUID();

            // WHEN
            repository.saveResult(testKey, expectedResult);

            // THEN
            Optional<String> result = repository.getResult(testKey);
            assertThat(result).isPresent().hasValue(expectedResult);
        }

        @Test
        @DisplayName("PROCESSING 상태가 결과로 교체됨")
        void saveResult_shouldReplaceProcessingState() {
            // GIVEN - PROCESSING 상태 (getResult는 empty 반환)
            repository.tryAcquire(testKey);
            assertThat(repository.getResult(testKey)).isEmpty();

            // WHEN
            repository.saveResult(testKey, "completed");

            // THEN - 결과가 저장되어 조회 가능
            assertThat(repository.getResult(testKey)).isPresent().hasValue("completed");
        }
    }

    @Nested
    @DisplayName("getResult")
    class GetResultTest {

        @Test
        @DisplayName("존재하지 않는 키는 empty 반환")
        void nonExistentKey_shouldReturnEmpty() {
            // WHEN
            Optional<String> result = repository.getResult("non-existent-key");

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("PROCESSING 상태인 키는 empty 반환")
        void processingKey_shouldReturnEmpty() {
            // GIVEN
            repository.tryAcquire(testKey);

            // WHEN
            Optional<String> result = repository.getResult(testKey);

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("remove")
    class RemoveTest {

        @Test
        @DisplayName("키 삭제 후 재시도 가능")
        void remove_shouldAllowRetry() {
            // GIVEN
            repository.tryAcquire(testKey);

            // WHEN
            repository.remove(testKey);
            AcquireResult result = repository.tryAcquire(testKey);

            // THEN
            assertThat(result).isEqualTo(AcquireResult.ACQUIRED);
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("동시에 100개 요청 시 정확히 1개만 ACQUIRED")
        void concurrentRequests_onlyOneAcquired() throws InterruptedException {
            // GIVEN
            int threadCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            AtomicInteger acquiredCount = new AtomicInteger(0);
            AtomicInteger processingCount = new AtomicInteger(0);
            AtomicInteger completedCount = new AtomicInteger(0);

            String concurrentKey = "concurrent-" + UUID.randomUUID();

            // WHEN
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        AcquireResult result = repository.tryAcquire(concurrentKey);
                        switch (result) {
                            case ACQUIRED -> acquiredCount.incrementAndGet();
                            case PROCESSING -> processingCount.incrementAndGet();
                            case ALREADY_COMPLETED -> completedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
            executor.shutdown();

            // THEN
            assertThat(acquiredCount.get()).isEqualTo(1);
            assertThat(processingCount.get()).isEqualTo(threadCount - 1);
            assertThat(completedCount.get()).isZero();
        }

        @Test
        @DisplayName("처리 완료 후 동시 요청은 모두 ALREADY_COMPLETED")
        void concurrentRequestsAfterCompletion_allCompleted() throws InterruptedException {
            // GIVEN
            repository.tryAcquire(testKey);
            repository.saveResult(testKey, "completed");

            int threadCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            AtomicInteger completedCount = new AtomicInteger(0);

            // WHEN
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        AcquireResult result = repository.tryAcquire(testKey);
                        if (result == AcquireResult.ALREADY_COMPLETED) {
                            completedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
            executor.shutdown();

            // THEN
            assertThat(completedCount.get()).isEqualTo(threadCount);
        }
    }
}
