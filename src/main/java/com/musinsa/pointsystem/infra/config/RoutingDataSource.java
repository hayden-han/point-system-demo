package com.musinsa.pointsystem.infra.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DataSourceContextHolder.getDataSourceType();
        return dataSourceType != null ? dataSourceType : DataSourceType.PRIMARY;
    }
}
