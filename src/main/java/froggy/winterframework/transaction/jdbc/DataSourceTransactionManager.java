package froggy.winterframework.transaction.jdbc;

import froggy.winterframework.transaction.TransactionException;
import froggy.winterframework.transaction.TransactionManager;
import froggy.winterframework.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataSourceTransactionManager implements TransactionManager {

    private static final String BEGIN_FAILURE_MESSAGE = "Failed to begin transaction";
    private static final String COMMIT_FAILURE_MESSAGE = "Failed to commit transaction";
    private static final String ROLLBACK_FAILURE_MESSAGE = "Failed to rollback transaction";
    private static final String CLEANUP_FAILURE_MESSAGE = "Failed to cleanup transaction connection";
    private static final String INVALID_STATUS_MESSAGE = "Invalid transaction status";
    private static final String COMPLETED_STATUS_MESSAGE = "Transaction status is already completed";

    private final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null");
        }

        this.dataSource = dataSource;
    }

    @Override
    public TransactionStatus begin() throws TransactionException {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            boolean mustRestoreAutoCommit = connection.getAutoCommit();
            if (mustRestoreAutoCommit) {
                connection.setAutoCommit(false);
            }

            return new JdbcTransactionStatus(connection, mustRestoreAutoCommit);
        } catch (RuntimeException | SQLException e) {
            TransactionException failure = toTransactionException(BEGIN_FAILURE_MESSAGE, e);
            throw mergeFailures(failure, closeIfNecessary(connection));
        }
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        JdbcTransactionStatus jdbcStatus = getRequiredActiveJdbcStatus(status);
        completeTransaction(jdbcStatus, () -> jdbcStatus.connection.commit(), COMMIT_FAILURE_MESSAGE);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        JdbcTransactionStatus jdbcStatus = getRequiredActiveJdbcStatus(status);
        completeTransaction(jdbcStatus, () -> jdbcStatus.connection.rollback(), ROLLBACK_FAILURE_MESSAGE);
    }

    private void completeTransaction(
            JdbcTransactionStatus status,
            JdbcAction action,
            String failureMessage
    ) {
        TransactionException failure = null;
        try {
            action.execute();
        } catch (RuntimeException | SQLException e) {
            failure = toTransactionException(failureMessage, e);
        }

        failure = mergeFailures(failure, cleanup(status));
        status.markTransactionCompleted();
        if (failure != null) {
            throw failure;
        }
    }

    private TransactionException cleanup(JdbcTransactionStatus status) {
        TransactionException failure = null;

        if (status.mustRestoreAutoCommit) {
            try {
                status.connection.setAutoCommit(true);
            } catch (RuntimeException | SQLException e) {
                failure = toTransactionException(CLEANUP_FAILURE_MESSAGE, e);
            }
        }

        return mergeFailures(failure, close(status.connection));
    }

    private TransactionException mergeFailures(
            TransactionException primaryFailure,
            TransactionException secondaryFailure
    ) {
        if (secondaryFailure == null) {
            return primaryFailure;
        }

        if (primaryFailure == null) {
            return secondaryFailure;
        }

        if (primaryFailure != secondaryFailure) {
            primaryFailure.addSuppressed(secondaryFailure);
        }
        return primaryFailure;
    }

    private TransactionException closeIfNecessary(Connection connection) {
        if (connection == null) {
            return null;
        }

        return close(connection);
    }

    private TransactionException close(Connection connection) {
        try {
            connection.close();
            return null;
        } catch (RuntimeException | SQLException e) {
            return toTransactionException(CLEANUP_FAILURE_MESSAGE, e);
        }
    }

    private JdbcTransactionStatus getRequiredActiveJdbcStatus(TransactionStatus status) {
        if (!(status instanceof JdbcTransactionStatus)) {
            String statusType = status == null ? "null" : status.getClass().getName();
            throw new IllegalArgumentException(INVALID_STATUS_MESSAGE + ": " + statusType);
        }

        JdbcTransactionStatus jdbcStatus = (JdbcTransactionStatus) status;
        if (jdbcStatus.isTransactionCompleted()) {
            throw new IllegalArgumentException(COMPLETED_STATUS_MESSAGE);
        }

        return jdbcStatus;
    }

    private TransactionException toTransactionException(String message, Exception exception) {
        if (exception instanceof TransactionException) {
            return (TransactionException) exception;
        }

        return new TransactionException(message, exception);
    }

    private interface JdbcAction {
        void execute() throws SQLException;
    }

    private static final class JdbcTransactionStatus implements TransactionStatus {

        private final Connection connection;
        private final boolean mustRestoreAutoCommit;
        private boolean transactionCompleted;

        private JdbcTransactionStatus(Connection connection, boolean mustRestoreAutoCommit) {
            this.connection = connection;
            this.mustRestoreAutoCommit = mustRestoreAutoCommit;
        }

        private boolean isTransactionCompleted() {
            return transactionCompleted;
        }

        private void markTransactionCompleted() {
            transactionCompleted = true;
        }

    }
}
