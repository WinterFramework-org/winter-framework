package froggy.winterframework.aop.framework;

import froggy.winterframework.aop.MethodInterceptor;
import java.util.List;

public interface ProxyFactory {

    Object createProxy(Object target, List<MethodInterceptor> interceptors);

}
