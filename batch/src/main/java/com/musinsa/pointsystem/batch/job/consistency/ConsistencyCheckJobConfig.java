package com.musinsa.pointsystem.batch.job.consistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 포인트 정합성 검증 Job
 *
 * <p>검증 항목:</p>
 * <ul>
 *   <li>point_ledger.available_amount = entries 기반 계산값</li>
 *   <li>point_ledger.used_amount = USE entries - USE_CANCEL entries</li>
 * </ul>
 *
 * <p>실행 방법:</p>
 * <pre>
 * java -jar batch.jar --spring.batch.job.name=consistencyCheckJob
 * </pre>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConsistencyCheckJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final ConsistencyCheckProcessor processor;
    private final ConsistencyCheckWriter writer;

    @Value("${batch-job.consistency-check.chunk-size:1000}")
    private int chunkSize;

    @Value("${batch-job.consistency-check.page-size:1000}")
    private int pageSize;

    @Bean
    public Job consistencyCheckJob() {
        return new JobBuilder("consistencyCheckJob", jobRepository)
                .start(consistencyCheckStep())
                .listener(new ConsistencyCheckJobListener())
                .build();
    }

    @Bean
    public Step consistencyCheckStep() {
        return new StepBuilder("consistencyCheckStep", jobRepository)
                .<LedgerConsistencyDto, ConsistencyCheckResult>chunk(chunkSize, transactionManager)
                .reader(ledgerItemReader())
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(100)
                .skip(Exception.class)
                .listener(new ConsistencyCheckStepListener())
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<LedgerConsistencyDto> ledgerItemReader() {
        return new JdbcPagingItemReaderBuilder<LedgerConsistencyDto>()
                .name("ledgerItemReader")
                .dataSource(dataSource)
                .selectClause("""
                    SELECT
                        l.id,
                        l.member_id,
                        l.earned_amount,
                        l.available_amount,
                        l.used_amount,
                        l.is_canceled,
                        COALESCE(SUM(CASE WHEN e.type = 'EARN' THEN e.amount ELSE 0 END), 0) as entry_earn_sum,
                        COALESCE(SUM(CASE WHEN e.type = 'EARN_CANCEL' THEN e.amount ELSE 0 END), 0) as entry_earn_cancel_sum,
                        COALESCE(SUM(CASE WHEN e.type = 'USE' THEN e.amount ELSE 0 END), 0) as entry_use_sum,
                        COALESCE(SUM(CASE WHEN e.type = 'USE_CANCEL' THEN e.amount ELSE 0 END), 0) as entry_use_cancel_sum
                    """)
                .fromClause("FROM point_ledger l LEFT JOIN ledger_entry e ON l.id = e.ledger_id")
                .groupClause("GROUP BY l.id, l.member_id, l.earned_amount, l.available_amount, l.used_amount, l.is_canceled")
                .sortKeys(Map.of("l.id", Order.ASCENDING))
                .pageSize(pageSize)
                .rowMapper((rs, rowNum) -> new LedgerConsistencyDto(
                        rs.getBytes("id"),
                        rs.getBytes("member_id"),
                        rs.getLong("earned_amount"),
                        rs.getLong("available_amount"),
                        rs.getLong("used_amount"),
                        rs.getBoolean("is_canceled"),
                        rs.getLong("entry_earn_sum"),
                        rs.getLong("entry_earn_cancel_sum"),
                        rs.getLong("entry_use_sum"),
                        rs.getLong("entry_use_cancel_sum")
                ))
                .build();
    }
}
