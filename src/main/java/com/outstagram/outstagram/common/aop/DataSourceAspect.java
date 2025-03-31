package com.outstagram.outstagram.common.aop;

import com.outstagram.outstagram.config.database.DatabaseContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Aspect
@Component
@Order(0)
public class DataSourceAspect {
    @Around("@annotation(com.outstagram.outstagram.common.annotation.Master)")
    public Object routeToMaster(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();

        try {
            if (!transactionActive) {
                DatabaseContextHolder.setDatabaseType(DatabaseContextHolder.DatabaseType.MASTER);
            }
            return joinPoint.proceed();
        } finally {
            if (!transactionActive) {
                DatabaseContextHolder.clearDatabaseType();
            }
        }
    }

    @Around("@annotation(com.outstagram.outstagram.common.annotation.Slave)")
    public Object routeToSlave(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();

        try {
            if (!transactionActive) {
                DatabaseContextHolder.setDatabaseType(DatabaseContextHolder.DatabaseType.SLAVE);
            }
            return joinPoint.proceed();
        } finally {
            if (!transactionActive) {
                DatabaseContextHolder.clearDatabaseType();
            }
        }
    }
}
