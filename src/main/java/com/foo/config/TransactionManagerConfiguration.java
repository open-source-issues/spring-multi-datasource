package com.foo.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizer;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
class TransactionManagerConfiguration {

    @Bean
    public TransactionManagerCustomizer<AbstractPlatformTransactionManager> configureFailForWriteTransactionsInReadOnlyTransactions() {
        return (AbstractPlatformTransactionManager transactionManager) -> transactionManager
                .setValidateExistingTransaction(true);
    }

    // Bug in Spring 3.2.0:
    // org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizationAutoConfiguration.platformTransactionManagerCustomizers
    // creates TransactionManagerCustomizers only if the PlatformTransactionManager
    // is available
    // But
    // org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.JdbcTransactionManagerConfiguration.transactionManager
    // creates the PlatformTransactionManager and uses the
    // TransactionManagerCustomizers which are not created
    // because they depend on the PlatformTransactionManager
    @Bean
    TransactionManagerCustomizers platformTransactionManagerCustomizers(
            ObjectProvider<TransactionManagerCustomizer<?>> customizers) {
        return TransactionManagerCustomizers.of(customizers.orderedStream().toList());
    }
}
