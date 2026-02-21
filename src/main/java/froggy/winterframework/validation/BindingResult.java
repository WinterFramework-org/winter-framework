package froggy.winterframework.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 검증 오류를 저장하는 결과 객체.
 */
public class BindingResult {

    private final Object target;
    private final String objectName;
    private final List<FieldError> fieldErrors = new ArrayList<>();
    private final List<ObjectError> globalErrors = new ArrayList<>();

    public BindingResult(Object target) {
        this(target, target != null ? target.getClass().getSimpleName() : "target");
    }

    public BindingResult(Object target, String objectName) {
        this.target = target;
        this.objectName = normalizeObjectName(objectName);
    }

    /**
     * 필드 오류를 추가한다.
     *
     * @param field 필드 경로
     * @param rejectedValue 거부된 값
     * @param code 오류 코드
     * @param message 사용자 메시지
     */
    public void addFieldError(String field, Object rejectedValue, String code, String message) {
        fieldErrors.add(new FieldError(objectName, field, rejectedValue, code, message));
    }

    /**
     * 필드 오류를 코드 없이 추가한다.
     *
     * @param field 필드 경로
     * @param rejectedValue 거부된 값
     * @param message 사용자 메시지
     */
    public void addFieldError(String field, Object rejectedValue, String message) {
        addFieldError(field, rejectedValue, null, message);
    }

    /**
     * 글로벌 오류를 추가한다.
     *
     * @param code 오류 코드
     * @param message 사용자 메시지
     */
    public void addGlobalError(String code, String message) {
        globalErrors.add(new ObjectError(objectName, code, message));
    }

    /**
     * 글로벌 오류를 코드 없이 추가한다.
     *
     * @param message 사용자 메시지
     */
    public void addGlobalError(String message) {
        addGlobalError(null, message);
    }

    public Object getTarget() {
        return target;
    }

    public String getObjectName() {
        return objectName;
    }

    public List<FieldError> getFieldErrors() {
        return Collections.unmodifiableList(fieldErrors);
    }

    public List<ObjectError> getGlobalErrors() {
        return Collections.unmodifiableList(globalErrors);
    }

    /**
     * 전체 오류를 반환한다.
     *
     * @return 글로벌 오류 + 필드 오류 목록
     */
    public List<ObjectError> getAllErrors() {
        List<ObjectError> allErrors = new ArrayList<>(globalErrors.size() + fieldErrors.size());
        allErrors.addAll(globalErrors);
        allErrors.addAll(fieldErrors);
        return Collections.unmodifiableList(allErrors);
    }

    public boolean hasErrors() {
        return !fieldErrors.isEmpty() || !globalErrors.isEmpty();
    }

    public int getErrorCount() {
        return fieldErrors.size() + globalErrors.size();
    }

    private String normalizeObjectName(String candidate) {
        if (candidate == null || candidate.trim().isEmpty()) {
            return "target";
        }
        return candidate;
    }

    /**
     * 객체 단위(글로벌) 오류 정보.
     */
    public static class ObjectError {

        private final String objectName;
        private final String code;
        private final String message;

        public ObjectError(String objectName, String code, String message) {
            this.objectName = objectName;
            this.code = code;
            this.message = message;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 필드 단위 오류 정보.
     */
    public static class FieldError extends ObjectError {

        private final String field;
        private final Object rejectedValue;

        public FieldError(String objectName, String field, Object rejectedValue, String code, String message) {
            super(objectName, code, message);
            this.field = field;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
}
