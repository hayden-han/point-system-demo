package com.musinsa.pointsystem.presentation.exception;

import com.musinsa.pointsystem.application.exception.LockAcquisitionFailedException;
import com.musinsa.pointsystem.domain.exception.*;
import com.musinsa.pointsystem.presentation.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final int RETRY_AFTER_SECONDS = 3;

    @ExceptionHandler(PointLedgerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePointLedgerNotFound(PointLedgerNotFoundException e) {
        log.warn("적립건을 찾을 수 없음: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("LEDGER_NOT_FOUND", e.getUserMessage()));
    }

    @ExceptionHandler(PointTransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePointTransactionNotFound(PointTransactionNotFoundException e) {
        log.warn("트랜잭션을 찾을 수 없음: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("TRANSACTION_NOT_FOUND", e.getUserMessage()));
    }

    @ExceptionHandler(MemberPointNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberPointNotFound(MemberPointNotFoundException e) {
        log.warn("회원 포인트를 찾을 수 없음: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("MEMBER_POINT_NOT_FOUND", e.getUserMessage()));
    }

    @ExceptionHandler(InsufficientPointException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPoint(InsufficientPointException e) {
        log.warn("포인트 부족: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INSUFFICIENT_POINT", e.getUserMessage()));
    }

    @ExceptionHandler(InvalidEarnAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEarnAmount(InvalidEarnAmountException e) {
        log.warn("유효하지 않은 적립 금액: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_EARN_AMOUNT", e.getUserMessage()));
    }

    @ExceptionHandler(MaxBalanceExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxBalanceExceeded(MaxBalanceExceededException e) {
        log.warn("최대 보유 금액 초과: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("MAX_BALANCE_EXCEEDED", e.getUserMessage()));
    }

    @ExceptionHandler(InvalidExpirationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidExpiration(InvalidExpirationException e) {
        log.warn("유효하지 않은 만료일: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_EXPIRATION", e.getUserMessage()));
    }

    @ExceptionHandler(PointLedgerAlreadyCanceledException.class)
    public ResponseEntity<ErrorResponse> handlePointLedgerAlreadyCanceled(PointLedgerAlreadyCanceledException e) {
        log.warn("이미 취소된 적립건: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("LEDGER_ALREADY_CANCELED", e.getUserMessage()));
    }

    @ExceptionHandler(PointLedgerAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handlePointLedgerAlreadyUsed(PointLedgerAlreadyUsedException e) {
        log.warn("이미 사용된 적립건: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("LEDGER_ALREADY_USED", e.getUserMessage()));
    }

    @ExceptionHandler(InvalidCancelAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCancelAmount(InvalidCancelAmountException e) {
        log.warn("유효하지 않은 취소 금액: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_CANCEL_AMOUNT", e.getUserMessage()));
    }

    @ExceptionHandler(InvalidOrderIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderId(InvalidOrderIdException e) {
        log.warn("유효하지 않은 주문번호: {}", e.getInternalMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("INVALID_ORDER_ID", e.getUserMessage()));
    }

    @ExceptionHandler(LockAcquisitionFailedException.class)
    public ResponseEntity<ErrorResponse> handleLockAcquisitionFailed(LockAcquisitionFailedException e) {
        log.error("분산락 획득 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(RETRY_AFTER_SECONDS))
                .body(ErrorResponse.of("LOCK_ACQUISITION_FAILED",
                        "서버가 바쁩니다. " + RETRY_AFTER_SECONDS + "초 후 다시 시도해주세요."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("유효성 검증 실패: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRequest(DuplicateRequestException e) {
        log.warn("중복 요청 감지: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of("DUPLICATE_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}
