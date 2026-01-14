package com.musinsa.pointsystem.presentation.controller;

import com.musinsa.pointsystem.infra.lock.LockInfo;
import com.musinsa.pointsystem.infra.lock.LockManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 분산락 관리 API (운영용)
 * - 프로덕션 환경에서는 접근 제어 필요 (Spring Security, IP 제한 등)
 */
@RestController
@RequestMapping("/admin/locks")
@RequiredArgsConstructor
@Tag(name = "Lock Admin", description = "분산락 관리 API (운영용)")
public class LockAdminController {

    private final LockManagementService lockManagementService;

    @GetMapping("/{lockKey}")
    @Operation(summary = "락 상태 조회", description = "특정 락의 상태를 조회합니다.")
    public ResponseEntity<LockInfo> getLockInfo(@PathVariable String lockKey) {
        LockInfo lockInfo = lockManagementService.getLockInfo(lockKey);
        return ResponseEntity.ok(lockInfo);
    }

    @PostMapping("/{lockKey}/force-unlock")
    @Operation(summary = "락 강제 해제", description = "특정 락을 강제로 해제합니다. 주의: 데이터 정합성에 영향을 줄 수 있습니다.")
    public ResponseEntity<Map<String, Object>> forceUnlock(@PathVariable String lockKey) {
        boolean unlocked = lockManagementService.forceUnlock(lockKey);
        return ResponseEntity.ok(Map.of(
                "lockKey", lockKey,
                "unlocked", unlocked,
                "message", unlocked ? "락이 강제 해제되었습니다." : "락이 이미 해제된 상태입니다."
        ));
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "회원 포인트 락 상태 조회", description = "특정 회원의 포인트 락 상태를 조회합니다.")
    public ResponseEntity<LockInfo> getMemberPointLockInfo(@PathVariable String memberId) {
        LockInfo lockInfo = lockManagementService.getMemberPointLockInfo(memberId);
        return ResponseEntity.ok(lockInfo);
    }

    @PostMapping("/members/{memberId}/force-unlock")
    @Operation(summary = "회원 포인트 락 강제 해제", description = "특정 회원의 포인트 락을 강제로 해제합니다.")
    public ResponseEntity<Map<String, Object>> forceUnlockMemberPoint(@PathVariable String memberId) {
        boolean unlocked = lockManagementService.forceUnlockMemberPoint(memberId);
        String lockKey = "lock:point:member:" + memberId;
        return ResponseEntity.ok(Map.of(
                "memberId", memberId,
                "lockKey", lockKey,
                "unlocked", unlocked,
                "message", unlocked ? "락이 강제 해제되었습니다." : "락이 이미 해제된 상태입니다."
        ));
    }
}
