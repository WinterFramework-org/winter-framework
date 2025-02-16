package froggy.winterframework.utils.convert;

/**
 * 특정 타입으로 변환하는 인터페이스.
 */
public interface TypeConverter {

    /**
     * 해당 타입을 변환할 수 있는지 확인.
     *
     * @param clazz 변환할 대상 클래스
     * @return 지원 여부
     */
    public Boolean isSupport(Class<?> clazz);

    /**
     *  주어진 값을 특정 타입으로 변환.
     *
     * @param targetType 변환할 타입
     * @param value 변환할 값
     * @param <T> 변환할 타입의 제네릭
     * @return 변환된 객체
     */
    public <T> T convert(Class<T> targetType, Object value);
}
