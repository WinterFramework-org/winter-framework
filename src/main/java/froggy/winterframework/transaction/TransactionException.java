package froggy.winterframework.transaction;

/**
 * transaction 처리 중 발생한 실패를 나타내는 runtime exception.
 */
public class TransactionException extends RuntimeException {

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
