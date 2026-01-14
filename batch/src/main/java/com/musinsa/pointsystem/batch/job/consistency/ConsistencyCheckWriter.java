package com.musinsa.pointsystem.batch.job.consistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 정합성 검증 결과 Writer
 * - 불일치 항목을 별도 테이블에 기록
 * - 알림 발송 (선택적)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsistencyCheckWriter implements ItemWriter<ConsistencyCheckResult> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends ConsistencyCheckResult> chunk) {
        List<? extends ConsistencyCheckResult> inconsistentItems = chunk.getItems().stream()
                .filter(result -> !result.isConsistent())
                .toList();

        if (inconsistentItems.isEmpty()) {
            return;
        }

        log.info("Writing {} inconsistent records", inconsistentItems.size());

        // 정합성 불일치 레코드 저장
        String sql = """
            INSERT INTO consistency_check_result (ledger_id, member_id, inconsistency_type, details, detected_at)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                inconsistency_type = VALUES(inconsistency_type),
                details = VALUES(details),
                detected_at = VALUES(detected_at)
            """;

        LocalDateTime now = LocalDateTime.now();

        for (ConsistencyCheckResult result : inconsistentItems) {
            jdbcTemplate.update(sql,
                    uuidToBytes(result.ledgerId()),
                    uuidToBytes(result.memberId()),
                    result.type().name(),
                    result.details(),
                    now
            );
        }
    }

    private byte[] uuidToBytes(java.util.UUID uuid) {
        if (uuid == null) {
            return null;
        }
        byte[] bytes = new byte[16];
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
}
