package com.musinsa.pointsystem.domain.exception;

import java.util.UUID;

public class MemberPointNotFoundException extends PointException {

    public MemberPointNotFoundException(UUID memberId) {
        super("회원 포인트를 찾을 수 없습니다. memberId=" + memberId);
    }
}
