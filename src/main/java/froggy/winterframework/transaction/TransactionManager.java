package froggy.winterframework.transaction;

/**
 * transaction을 시작하고 commit/rollback으로 종료하는 manager.
 */
public interface TransactionManager {

    TransactionStatus begin() throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}
