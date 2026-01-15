# 포인트 시스템 (Point System)

무료 포인트의 적립, 적립취소, 사용, 사용취소 기능을 제공하는 API 시스템입니다.

## 빌드 및 실행

### 사전 요구사항

- JDK 21 이상
- Gradle 8.x (또는 Gradle Wrapper 사용)

### 빌드

```bash
./gradlew build
```

### 테스트

```bash
./gradlew test
```

### 실행

```bash
./gradlew :app:bootRun
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다.

### API 문서 (Swagger)

```
http://localhost:8080/swagger-ui.html
```

## 기술 스택

| 기술 | 버전 | 설명 |
|------|------|------|
| Java | 21 | 프로그래밍 언어 |
| Spring Boot | 3.5.x | 웹 프레임워크 |
| Spring Data JPA | - | ORM |
| QueryDSL | 5.1.0 | 타입 안전 쿼리 |
| H2 Database | - | 인메모리 DB (개발/테스트) |
| Redis (Redisson) | - | 분산락, 멱등성 캐시 |
| JUnit 5 | - | 테스트 프레임워크 |

---

## 아키텍처

### 클린 아키텍처 (Clean Architecture)

이 프로젝트는 Uncle Bob의 클린 아키텍처를 기반으로 설계되었습니다.

```
┌──────────────────────────────────────────────────────────────┐
│  Presentation                                                │
│  (Controller, DTO, ExceptionHandler)                         │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  Application                                                 │
│  (UseCase, Command/Result DTO)                               │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  Domain                                                      │
│  (Entity, VO, Repository Interface, Domain Service, Rules)  │
└──────────────────────────▲───────────────────────────────────┘
                           │
                           │
┌──────────────────────────┴───────────────────────────────────┐
│  Infrastructure                                              │
│  (JPA Entity, Repository Impl, Redis, Event Handler)         │
└──────────────────────────────────────────────────────────────┘
```

**의존성 방향**: `Presentation → Application → Domain ← Infrastructure`

- **Domain(중심)**: 어떤 외부 레이어도 알지 못함. 순수 비즈니스 로직만 포함
- **Application**: Domain에만 의존. 유스케이스 오케스트레이션 담당
- **Presentation**: Application을 통해 Domain에 접근. 요청/응답 변환
- **Infrastructure**: Domain의 인터페이스를 구현. DIP(의존성 역전)를 통해 Domain을 향해 의존

### 설계 의사결정: DDD Aggregate 패턴 적용 및 원복

초기에 DDD의 Aggregate 패턴을 적용하여 `MemberPoint`를 Aggregate Root로 설계했으나, 다음과 같은 이유로 현재의 단순한 구조로 원복했습니다.

**기존 DDD 구조 (MemberPoint Aggregate)**
```
MemberPoint (Aggregate Root)
├── List<PointLedger>     # 적립 원장 목록
└── List<LedgerEntry>     # 변동 이력 목록
```

**원복 이유:**

1. **메모리 효율성 문제**
   - 회원이 수백~수천 개의 적립건을 보유할 수 있음
   - Aggregate 로드 시 모든 Ledger와 Entry를 메모리에 적재해야 함
   - 특히 잔액 조회 같은 단순 연산에도 전체 데이터 로드 필요

2. **성능 최적화의 어려움**
   - DDD 원칙상 Aggregate 경계를 넘는 쿼리 최적화가 제한됨
   - `SUM(available_amount)` 같은 DB 수준 최적화 적용 불가
   - N+1 문제 회피를 위한 복잡한 Fetch 전략 필요

3. **실용적 트레이드오프**
   - 포인트 시스템은 CRUD 중심의 단순한 도메인
   - 복잡한 비즈니스 불변조건이 적어 Aggregate의 이점이 제한적
   - 현재 구조에서도 `PointRules`로 비즈니스 규칙을 명확히 캡슐화

**현재 구조의 장점:**
- `PointLedger`와 `LedgerEntry`를 독립적으로 조회/저장
- DB 수준의 쿼리 최적화 자유로움 (SUM, JOIN, 페이징)
- 메모리 사용량 최소화
- 단순하고 이해하기 쉬운 코드


### 프로젝트 구조
#### 멀티 모듈 구조
```
musinsa-point-system/
├── domain/          # 순수 도메인 (Spring 의존성 없음)
├── infra/           # 인프라 구현 (JPA, Redis, 설정)
├── app/             # REST API 애플리케이션
└── batch/           # 배치 작업
```
batch 작업을 위한 모듈을 만들면서 도메인 계층을 양측이 공유할 필요가 생겨 domain도 별도의 모듈로 분리

### 모듈 의존성

```mermaid
graph BT
    app --> domain
    app --> infra
    batch --> domain
    batch --> infra
    infra --> domain
```

| 모듈 | 의존 대상 | 설명 |
|------|----------|------|
| `domain` | 없음 | 순수 Java, 외부 의존성 없음 |
| `infra` | `domain` | Repository 인터페이스 구현 |
| `app` | `domain`, `infra` | UseCase + REST API |
| `batch` | `domain`, `infra` | 배치 작업 |

---

## 패키지 구조

### Domain 모듈 (`domain/`)

```
domain/
├── event/              # 도메인 이벤트
│   ├── PointEarnedEvent.java
│   ├── PointEarnCanceledEvent.java
│   ├── PointUsedEvent.java
│   └── PointUseCanceledEvent.java
├── exception/          # 도메인 예외
│   ├── InsufficientPointException.java
│   ├── InvalidEarnAmountException.java
│   └── ...
├── infrastructure/     # 기술적 포트 (Cross-Cutting Concern) - 실용적 접근
│   └── IdempotencyKeyPort.java
├── model/              # 엔티티, VO, 규칙
│   ├── PointLedger.java        # 적립 원장 (핵심 엔티티)
│   ├── LedgerEntry.java        # 변동 이력
│   ├── EarnType.java           # 적립 유형 (SYSTEM/MANUAL)
│   ├── PointAmount.java        # 포인트 금액 VO
│   ├── PointRules.java         # 비즈니스 규칙 (순수 함수)
│   └── DistributedLock.java    # 분산락 어노테이션
├── repository/         # 리포지토리 인터페이스
│   ├── PointLedgerRepository.java
│   ├── LedgerEntryRepository.java
│   ├── PointQueryRepository.java
│   ├── IdGenerator.java
│   ├── BalanceCachePort.java
│   └── PointEventPublisher.java
└── service/            # 도메인 서비스
    └── UseCancelProcessor.java   # 사용취소 처리 로직
```

### Infra 모듈 (`infra/`)

```
infra/
├── adapter/            # 어댑터 (포트 구현)
│   └── UuidGenerator.java
├── cache/              # 캐시 서비스
│   └── PointBalanceCacheService.java
├── config/             # 설정
│   ├── DomainConfig.java
│   ├── RedisCacheConfig.java
│   └── RoutingDataSource.java
├── event/              # 이벤트 핸들러
│   └── PointEventHandler.java
├── idempotency/        # 멱등성 구현
│   └── IdempotencyKeyRepository.java
├── lock/               # 분산락 구현
│   └── DistributedLockAspect.java
├── metrics/            # 메트릭 수집
│   └── LockMetrics.java
└── persistence/        # JPA 구현
    ├── entity/
    ├── mapper/
    └── repository/
```

### App 모듈 (`app/`)

```
app/
├── application/        # 애플리케이션 레이어
│   ├── config/
│   │   └── DomainServiceConfig.java
│   ├── dto/
│   │   ├── EarnPointCommand.java
│   │   ├── EarnPointResult.java
│   │   └── ...
│   └── usecase/
│       ├── EarnPointUseCase.java
│       ├── UsePointUseCase.java
│       ├── CancelEarnPointUseCase.java
│       ├── CancelUsePointUseCase.java
│       ├── GetPointBalanceUseCase.java
│       └── GetPointHistoryUseCase.java
└── presentation/       # 프레젠테이션 레이어
    ├── controller/
    ├── dto/
    ├── exception/
    └── support/
        └── IdempotencySupport.java
```

---

## 시퀀스 다이어그램

### 포인트 적립 (Earn)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant IdempotencySupport
    participant UseCase as EarnPointUseCase
    participant Lock as DistributedLock
    participant Repository
    participant EventPublisher
    participant EventHandler

    Client->>Controller: POST /points/earn
    Controller->>IdempotencySupport: execute()

    alt Idempotency Key 존재
        IdempotencySupport-->>Controller: 캐시된 응답 반환
    else 새 요청
        IdempotencySupport->>UseCase: execute(command)

        UseCase->>Lock: 분산락 획득 (memberId)
        Lock-->>UseCase: 락 획득 성공

        UseCase->>Repository: 현재 잔액 조회
        UseCase->>UseCase: PointRules.validateEarn()
        UseCase->>Repository: Ledger 저장
        UseCase->>Repository: Entry 저장
        UseCase->>EventPublisher: PointEarnedEvent 발행

        UseCase->>Lock: 락 해제
        UseCase-->>IdempotencySupport: EarnPointResult

        Note over EventHandler: 트랜잭션 커밋 후
        EventPublisher->>EventHandler: handlePointEarned()
        EventHandler->>EventHandler: 캐시 무효화

        IdempotencySupport-->>Controller: EarnPointResponse
    end

    Controller-->>Client: 200 OK
```

### 포인트 사용 (Use)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase as UsePointUseCase
    participant Lock as DistributedLock
    participant Repository
    participant EventHandler

    Client->>Controller: POST /points/use
    Controller->>UseCase: execute(command)

    UseCase->>Lock: 분산락 획득 (memberId)
    Lock-->>UseCase: 락 획득 성공

    UseCase->>Repository: 사용 가능한 Ledger 조회
    UseCase->>UseCase: PointRules.validateSufficientBalance()
    UseCase->>UseCase: PointRules.getAvailableLedgersSorted()

    Note over UseCase: MANUAL 우선, 만료일 짧은 순

    loop 각 Ledger에서 차감
        UseCase->>UseCase: 차감 금액 계산
        UseCase->>Repository: Ledger 업데이트
        UseCase->>Repository: USE Entry 저장
    end

    UseCase->>UseCase: PointUsedEvent 발행
    UseCase->>Lock: 락 해제

    Note over EventHandler: 트랜잭션 커밋 후
    EventHandler->>EventHandler: 캐시 무효화

    UseCase-->>Controller: UsePointResult
    Controller-->>Client: 200 OK
```

### 포인트 사용취소 (Cancel Use)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase as CancelUsePointUseCase
    participant Processor as UseCancelProcessor
    participant Repository
    participant EventHandler

    Client->>Controller: POST /points/use/cancel
    Controller->>UseCase: execute(command)

    UseCase->>UseCase: 분산락 획득 (memberId)
    UseCase->>Repository: orderId로 관련 Ledger 조회
    UseCase->>Repository: 관련 Entry 조회

    UseCase->>Processor: calculateCancelableAmount()
    Processor-->>UseCase: 취소 가능 금액

    UseCase->>UseCase: PointRules.validateCancelAmount()

    UseCase->>Processor: process()

    loop 각 Ledger 처리
        alt Ledger가 만료됨
            Processor->>Processor: 신규 Ledger 생성
            Processor->>Processor: EARN Entry 생성
        else Ledger가 유효함
            Processor->>Processor: Ledger.availableAmount 복구
        end
        Processor->>Processor: USE_CANCEL Entry 생성
    end

    Processor-->>UseCase: CancelResult

    UseCase->>Repository: 변경사항 저장
    UseCase->>UseCase: PointUseCanceledEvent 발행

    Note over EventHandler: 트랜잭션 커밋 후
    EventHandler->>EventHandler: 캐시 무효화

    UseCase-->>Controller: CancelUsePointResult
    Controller-->>Client: 200 OK
```

### 포인트 적립취소 (Cancel Earn)

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase as CancelEarnPointUseCase
    participant Repository
    participant EventHandler

    Client->>Controller: POST /points/earn/{ledgerId}/cancel
    Controller->>UseCase: execute(command)

    UseCase->>UseCase: 분산락 획득 (memberId)
    UseCase->>Repository: Ledger 조회

    UseCase->>UseCase: PointRules.validateCancelEarn()
    Note over UseCase: 전액 사용 가능한 경우만 취소 가능

    UseCase->>UseCase: ledger.withCanceled()
    UseCase->>Repository: Ledger 저장 (is_canceled=true)
    UseCase->>Repository: EARN_CANCEL Entry 저장
    UseCase->>UseCase: PointEarnCanceledEvent 발행

    Note over EventHandler: 트랜잭션 커밋 후
    EventHandler->>EventHandler: 캐시 무효화

    UseCase-->>Controller: CancelEarnPointResult
    Controller-->>Client: 200 OK
```

---

## 도메인 모델

### 핵심 개념

| 개념 | 설명 |
|------|------|
| **PointLedger** | 적립 원장. 각 적립건을 나타내는 핵심 엔티티 |
| **LedgerEntry** | 변동 이력. 모든 포인트 변동을 1원 단위로 추적 |
| **PointRules** | 비즈니스 규칙을 순수 함수로 정의 |

### Entry 타입

| 타입 | 설명 |
|------|------|
| `EARN` | 적립 |
| `EARN_CANCEL` | 적립 취소 |
| `USE` | 사용 |
| `USE_CANCEL` | 사용 취소 |

### 상태 다이어그램

```mermaid
stateDiagram-v2
    [*] --> Active: 포인트 적립
    Active --> Active: 포인트 사용
    Active --> Active: 사용 취소
    Active --> Canceled: 적립 취소
    Active --> Expired: 만료일 경과
    Canceled --> [*]
    Expired --> [*]

    note right of Active
        available_amount > 0
        is_canceled = false
        expired_at > now
    end note

    note right of Canceled
        is_canceled = true
    end note

    note right of Expired
        expired_at <= now
    end note
```

---

## 데이터베이스 스키마

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│  point_policy   │       │  point_ledger   │       │  ledger_entry   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │──────▶│ id (PK)         │
│ policy_key (UK) │       │ member_id       │       │ ledger_id (FK)  │
│ policy_value    │       │ earned_amount   │       │ type            │
│ description     │       │ available_amount│       │ amount          │
└─────────────────┘       │ earn_type       │       │ order_id        │
                          │ expired_at      │       │ created_at      │
                          │ is_canceled     │       └─────────────────┘
                          │ source_ledger_id│
                          │ created_at      │
                          └─────────────────┘
```

| 테이블 | 설명 |
|--------|------|
| `point_policy` | 정책 설정 (적립 한도, 만료일 등) |
| `point_ledger` | 적립 원장 (각 적립건) |
| `ledger_entry` | 변동 이력 (모든 포인트 변동 추적) |

---

## 주요 설계 결정

### 동시성 제어

- **Redis 분산락 (Redisson)**: 회원 단위 락으로 동시 요청 제어
- **재시도 정책**: 0.2초, 0.5초, 1초 간격 최대 3회
- **타임아웃**: 락 대기 3초, 자동 해제 5초

### 멱등성 보장

- **Idempotency-Key 헤더**: 클라이언트가 생성한 고유 키
- **Redis 캐시**: 결과 저장 (TTL 24시간)
  - 네트워크 장애로 인한 재시도는 대부분 수 초~수 분 내 발생하나, 클라이언트 배포/장애 복구 등 예외 상황을 고려한 보수적인 값
  - 트래픽과 메모리 사용량에 따라 1시간 등으로 단축 가능
- **3가지 상태**: ACQUIRED, PROCESSING, ALREADY_COMPLETED

### 캐시 무효화

- **트랜잭션 커밋 후 처리**: `@TransactionalEventListener(AFTER_COMMIT)`
- **이벤트 기반**: 도메인 이벤트 핸들러에서 캐시 무효화
- **데이터 정합성**: 트랜잭션 롤백 시 캐시 유지

### N+1 문제 방지

- **JPA 연관관계 없음**: PointLedger와 LedgerEntry 간 명시적 조회
- **배치 조회**: `findByLedgerIds()`로 여러 Ledger의 Entry 한 번에 조회
- **Entity/Domain 분리**: JPA Entity와 도메인 모델 명확히 분리

---

## API 명세

> **인증:** 모든 API는 `X-Member-Id` 헤더로 회원 ID를 전달받습니다.

### 1. 포인트 적립

**POST** `/api/v1/points/earn`

```bash
curl -X POST http://localhost:8080/api/v1/points/earn \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "earnType": "SYSTEM",
    "expirationDays": 365
  }'
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| amount | Long | O | 적립 금액 (1 ~ 100,000) |
| earnType | String | O | SYSTEM(시스템) / MANUAL(수기지급) |
| expirationDays | Integer | X | 만료일 (기본 365일, 최대 1824일) |

### 2. 포인트 적립취소

**POST** `/api/v1/points/earn/{ledgerId}/cancel`

```bash
curl -X POST http://localhost:8080/api/v1/points/earn/{ledgerId}/cancel \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000"
```

- 미사용 적립건만 취소 가능
- 전체 금액 취소만 가능 (부분 취소 불가)

### 3. 포인트 사용

**POST** `/api/v1/points/use`

```bash
curl -X POST http://localhost:8080/api/v1/points/use \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500,
    "orderId": "ORDER-12345"
  }'
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| amount | Long | O | 사용 금액 |
| orderId | String | O | 주문번호 |

**사용 우선순위:**
1. 수기 지급(MANUAL) 포인트 우선
2. 만료일이 짧은 순서

### 4. 포인트 사용취소

**POST** `/api/v1/points/use/cancel`

```bash
curl -X POST http://localhost:8080/api/v1/points/use/cancel \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-12345",
    "cancelAmount": 300
  }'
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | String | O | 원본 주문번호 |
| cancelAmount | Long | O | 취소 금액 |

- 전체 또는 부분 취소 가능
- 만료된 적립건은 신규 적립으로 복구

### 5. 잔액 조회

**GET** `/api/v1/points`

```bash
curl http://localhost:8080/api/v1/points \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000"
```

### 6. 이력 조회

**GET** `/api/v1/points/history?page=0&size=20`

```bash
curl "http://localhost:8080/api/v1/points/history?page=0&size=20" \
  -H "X-Member-Id: 550e8400-e29b-41d4-a716-446655440000"
```

---

## 설정값

| 설정        | 기본값           | 설명 |
|-----------|---------------|----|
| 1회 최소 적립  | 1원            |    |
| 1회 최대 적립  | 100,000원      |    |
| 최대 보유 한도  | 10,000,000원   |    |
| 기본 만료일    | 365일          |    |
| 최소 만료일    | 1일            |    |
| 최대 만료일    | 1,824일 (약 5년) |    |
| 멱등성 키 TTL | 24시간          |    |
| 분산락 대기 시간 | 3초            |    |
| 분산락 유지 시간 | 5초            |    |

---

## 테스트

### 테스트 구조

```
tests/
├── unit/               # 단위 테스트
│   ├── PointLedgerTest
│   ├── LedgerEntryTest
│   └── PointAmountTest
├── integration/        # 통합 테스트
│   ├── EarnPointUseCaseTest
│   ├── UsePointUseCaseTest
│   ├── CancelEarnPointUseCaseTest
│   └── CancelUsePointUseCaseTest
├── scenario/           # 시나리오 테스트
│   └── IntegrationScenarioTest
└── concurrency/        # 동시성 테스트
    └── ConcurrencyTest
```