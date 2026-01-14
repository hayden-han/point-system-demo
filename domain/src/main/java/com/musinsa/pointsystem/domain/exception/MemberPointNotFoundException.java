package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class MemberPointNotFoundException extends PointException {

    private static final String USER_MESSAGE = "회원 포인트 정보를 찾을 수 없습니다.";

    public MemberPointNotFoundException(UUID memberId) {
        super(USER_MESSAGE,
              String.format("회원 포인트 없음. memberId=%s", memberId));
    }
}
