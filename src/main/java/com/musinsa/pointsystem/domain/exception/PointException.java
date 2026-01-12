package com.musinsa.pointsystem.domain.exception;

/**
 * 포인트 도메인 예외의 기본 클래스.
 * 사용자에게 노출되는 메시지와 내부 로깅용 메시지를 분리합니다.
 */
public abstract class PointException extends RuntimeException {

    private final String userMessage;

    protected PointException(String userMessage, String internalMessage) {
        super(internalMessage);
        this.userMessage = userMessage;
    }

    protected PointException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    /**
     * 사용자에게 노출되는 메시지
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * 내부 로깅용 메시지 (상세 정보 포함)
     */
    public String getInternalMessage() {
        return getMessage();
    }
}
