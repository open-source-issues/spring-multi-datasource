package com.foo.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        var currentTransactionReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        log.debug(
                "Current transaction {} is readonly: {}",
                TransactionSynchronizationManager.getCurrentTransactionName(),
                currentTransactionReadOnly);
        return currentTransactionReadOnly ? DataSourceType.READ_ONLY : DataSourceType.READ_WRITE;
    }
}
