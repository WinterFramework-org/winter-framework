package froggy.winterframework.transaction;

public interface TransactionManager {

    TransactionStatus begin() throws TransactionException;

    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}
