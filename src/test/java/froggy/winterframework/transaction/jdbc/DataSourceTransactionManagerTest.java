package froggy.winterframework.transaction.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import froggy.winterframework.transaction.TransactionException;
import froggy.winterframework.transaction.TransactionStatus;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceTransactionManagerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DataSourceTransactionManager transactionManager;

    @Before
    public void setUp() {
        transactionManager = new DataSourceTransactionManager(dataSource);
    }

    @Test
    public void 생성자에_null_DataSource를_전달하면_예외를_던진다() {
        // Given
        DataSource nullDataSource = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> new DataSourceTransactionManager(nullDataSource)
        );

        // Then
        assertEquals("DataSource must not be null", actualException.getMessage());
    }

    @Test
    public void begin은_가져온_connection의_autoCommit이_true이면_false로_설정한다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);

        // When
        TransactionStatus status = transactionManager.begin();

        // Then
        assertNotNull(status);
        InOrder inOrder = inOrder(dataSource, connection);
        inOrder.verify(dataSource).getConnection();
        inOrder.verify(connection).getAutoCommit();
        inOrder.verify(connection).setAutoCommit(false);
    }

    @Test
    public void begin은_autoCommit이_이미_false인_connection을_그대로_사용한다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(false);

        // When
        TransactionStatus status = transactionManager.begin();

        // Then
        assertNotNull(status);
        verify(connection, never()).setAutoCommit(anyBoolean());
        verify(connection, never()).close();
    }

    @Test
    public void begin에서_getConnection이_실패하면_TransactionException으로_감싼다() throws SQLException {
        // Given
        SQLException sqlCause = new SQLException("connection failed");
        given(dataSource.getConnection()).willThrow(sqlCause);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertEquals("Failed to begin transaction", actualException.getMessage());
        assertSame(sqlCause, actualException.getCause());
        assertEquals(0, actualException.getSuppressed().length);
    }

    @Test
    public void begin에서_getAutoCommit이_실패하면_TransactionException으로_감싸고_connection을_close한다() throws SQLException {
        // Given
        SQLException sqlCause = new SQLException("autoCommit read failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willThrow(sqlCause);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertEquals("Failed to begin transaction", actualException.getMessage());
        assertSame(sqlCause, actualException.getCause());
        verify(connection).close();
    }

    @Test
    public void begin에서_setAutoCommit이_실패하면_TransactionException으로_감싸고_connection을_close한다() throws SQLException {
        // Given
        SQLException sqlCause = new SQLException("autoCommit disable failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        willThrow(sqlCause).given(connection).setAutoCommit(false);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertEquals("Failed to begin transaction", actualException.getMessage());
        assertSame(sqlCause, actualException.getCause());
        verify(connection).close();
    }

    @Test
    public void begin에서_runtime_exception이_발생하면_TransactionException으로_감싸고_connection을_close한다() throws SQLException {
        // Given
        RuntimeException runtimeCause = new IllegalStateException("autoCommit read failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willThrow(runtimeCause);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertEquals("Failed to begin transaction", actualException.getMessage());
        assertSame(runtimeCause, actualException.getCause());
        verify(connection).close();
    }

    @Test
    public void begin에서_TransactionException이_발생하면_중복으로_감싸지_않는다() throws SQLException {
        // Given
        TransactionException transactionFailure = new TransactionException("autoCommit read failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willThrow(transactionFailure);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertSame(transactionFailure, actualException);
        verify(connection).close();
    }

    @Test
    public void begin에서_setAutoCommit_실패_후_close도_실패하면_close_예외를_suppressed로_보관한다() throws SQLException {
        // Given
        SQLException setAutoCommitFailure = new SQLException("autoCommit disable failed");
        SQLException closeFailure = new SQLException("close failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        willThrow(setAutoCommitFailure).given(connection).setAutoCommit(false);
        willThrow(closeFailure).given(connection).close();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.begin()
        );

        // Then
        assertEquals("Failed to begin transaction", actualException.getMessage());
        assertSame(setAutoCommitFailure, actualException.getCause());
        assertEquals(1, actualException.getSuppressed().length);
        assertEquals(TransactionException.class, actualException.getSuppressed()[0].getClass());
        assertEquals("Failed to cleanup transaction connection", actualException.getSuppressed()[0].getMessage());
        assertSame(closeFailure, actualException.getSuppressed()[0].getCause());
    }

    @Test
    public void commit은_connection을_commit하고_autoCommit을_복원한_뒤_close한다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);

        // When
        transactionManager.commit(status);

        // Then
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
        verify(connection, never()).rollback();
    }

    @Test
    public void rollback은_connection을_rollback하고_autoCommit을_복원한_뒤_close한다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);

        // When
        transactionManager.rollback(status);

        // Then
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
        verify(connection, never()).commit();
    }

    @Test
    public void commit은_autoCommit_복원이_필요없으면_connection만_close한다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(false);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);

        // When
        transactionManager.commit(status);

        // Then
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
        verify(connection, never()).setAutoCommit(anyBoolean());
    }

    @Test
    public void commit에서_close만_실패하면_TransactionException으로_전파한다() throws SQLException {
        // Given
        SQLException closeFailure = new SQLException("close failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(closeFailure).given(connection).close();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to cleanup transaction connection", actualException.getMessage());
        assertSame(closeFailure, actualException.getCause());
        assertEquals(0, actualException.getSuppressed().length);
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    public void autoCommit_복원만_실패하면_TransactionException으로_감싸고_close한다() throws SQLException {
        // Given
        SQLException restoreFailure = new SQLException("restore failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(restoreFailure).given(connection).setAutoCommit(true);

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to cleanup transaction connection", actualException.getMessage());
        assertSame(restoreFailure, actualException.getCause());
        verify(connection).close();
    }

    @Test
    public void autoCommit_복원_실패_후_close도_실패하면_close_예외를_suppressed로_보관한다() throws SQLException {
        // Given
        SQLException restoreFailure = new SQLException("restore failed");
        SQLException closeFailure = new SQLException("close failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(restoreFailure).given(connection).setAutoCommit(true);
        willThrow(closeFailure).given(connection).close();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to cleanup transaction connection", actualException.getMessage());
        assertSame(restoreFailure, actualException.getCause());
        assertEquals(1, actualException.getSuppressed().length);
        assertEquals(TransactionException.class, actualException.getSuppressed()[0].getClass());
        assertEquals("Failed to cleanup transaction connection", actualException.getSuppressed()[0].getMessage());
        assertSame(closeFailure, actualException.getSuppressed()[0].getCause());
    }

    @Test
    public void commit이_실패하면_TransactionException으로_감싸고_autoCommit을_복원한_뒤_close한다() throws SQLException {
        // Given
        SQLException commitFailure = new SQLException("commit failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(commitFailure).given(connection).commit();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to commit transaction", actualException.getMessage());
        assertSame(commitFailure, actualException.getCause());
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    public void commit에서_runtime_exception이_발생하면_TransactionException으로_감싸고_autoCommit을_복원한_뒤_close한다() throws SQLException {
        // Given
        RuntimeException runtimeCause = new IllegalStateException("commit failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(runtimeCause).given(connection).commit();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to commit transaction", actualException.getMessage());
        assertSame(runtimeCause, actualException.getCause());
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    public void commit_실패_후_close도_실패하면_close_예외를_suppressed로_보관한다() throws SQLException {
        // Given
        SQLException commitFailure = new SQLException("commit failed");
        SQLException closeFailure = new SQLException("close failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(commitFailure).given(connection).commit();
        willThrow(closeFailure).given(connection).close();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Failed to commit transaction", actualException.getMessage());
        assertSame(commitFailure, actualException.getCause());
        assertEquals(1, actualException.getSuppressed().length);
        assertEquals(TransactionException.class, actualException.getSuppressed()[0].getClass());
        assertEquals("Failed to cleanup transaction connection", actualException.getSuppressed()[0].getMessage());
        assertSame(closeFailure, actualException.getSuppressed()[0].getCause());
    }

    @Test
    public void commit이_실패한_status를_rollback하면_예외를_던진다() throws SQLException {
        // Given
        SQLException commitFailure = new SQLException("commit failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(commitFailure).given(connection).commit();
        assertThrows(TransactionException.class, () -> transactionManager.commit(status));

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> transactionManager.rollback(status)
        );

        // Then
        assertEquals("Transaction status is already completed", actualException.getMessage());
    }

    @Test
    public void rollback이_실패하면_TransactionException으로_감싸고_autoCommit을_복원한_뒤_close한다() throws SQLException {
        // Given
        SQLException rollbackFailure = new SQLException("rollback failed");
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        clearInvocations(connection);
        willThrow(rollbackFailure).given(connection).rollback();

        // When
        TransactionException actualException = assertThrows(
            TransactionException.class,
            () -> transactionManager.rollback(status)
        );

        // Then
        assertEquals("Failed to rollback transaction", actualException.getMessage());
        assertSame(rollbackFailure, actualException.getCause());
        InOrder inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    public void commit이_완료된_status를_다시_commit하면_예외를_던진다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        transactionManager.commit(status);

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Transaction status is already completed", actualException.getMessage());
    }

    @Test
    public void rollback이_완료된_status를_다시_rollback하면_예외를_던진다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);
        TransactionStatus status = transactionManager.begin();
        transactionManager.rollback(status);

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> transactionManager.rollback(status)
        );

        // Then
        assertEquals("Transaction status is already completed", actualException.getMessage());
    }

    @Test
    public void commit에_null_status를_전달하면_예외를_던진다() {
        // Given
        TransactionStatus status = null;

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> transactionManager.commit(status)
        );

        // Then
        assertEquals("Invalid transaction status: null", actualException.getMessage());
    }

    @Test
    public void rollback에_다른_status_구현체를_전달하면_예외를_던진다() {
        // Given
        TransactionStatus status = new OtherTransactionStatus();

        // When
        IllegalArgumentException actualException = assertThrows(
            IllegalArgumentException.class,
            () -> transactionManager.rollback(status)
        );

        // Then
        assertEquals(
            "Invalid transaction status: " + OtherTransactionStatus.class.getName(),
            actualException.getMessage()
        );
    }

    @Test
    public void begin이_반환한_status는_connection을_public_API로_노출하지_않는다() throws SQLException {
        // Given
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.getAutoCommit()).willReturn(true);

        // When
        TransactionStatus status = transactionManager.begin();

        // Then
        NoSuchMethodException actualException = assertThrows(
            NoSuchMethodException.class,
            () -> status.getClass().getMethod("getConnection")
        );
        assertEquals(status.getClass().getName() + ".getConnection()", actualException.getMessage());
    }

    private static class OtherTransactionStatus implements TransactionStatus {
    }
}
