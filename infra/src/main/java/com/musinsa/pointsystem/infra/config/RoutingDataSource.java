package com.musinsa.pointsystem.infra.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Read/Write DataSource 라우팅
 *
 * Spring의 TransactionSynchronizationManager를 사용하여
 * 현재 트랜잭션의 readOnly 속성에 따라 DataSource를 결정
 *
 * - @Transactional(readOnly = true) → REPLICA
 * - @Transactional(readOnly = false) → PRIMARY
 *
 * @see <a href="https://vladmihalcea.com/read-write-read-only-transaction-routing-spring/">
 *      Vlad Mihalcea - Read-write and read-only transaction routing with Spring</a>
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? DataSourceType.REPLICA
                : DataSourceType.PRIMARY;
    }
}
