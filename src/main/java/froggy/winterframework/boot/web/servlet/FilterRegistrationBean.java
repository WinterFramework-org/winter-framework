package froggy.winterframework.boot.web.servlet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;

public class FilterRegistrationBean<T extends Filter> {
    private T filter;
    private Set<String> urlPatterns = new LinkedHashSet();
    private EnumSet<DispatcherType> dispatcherTypes;
    private int order = Integer.MAX_VALUE;

    public FilterRegistrationBean(T filter) {
        this.filter = filter;
    }

    public T getFilter() {
        return filter;
    }

    public void setFilter(T filter) {
        this.filter = filter;
    }

    public Set<String> getUrlPatterns() {
        return urlPatterns;
    }

    public void addUrlPatterns(String... urlPatterns) {
        Collections.addAll(this.urlPatterns, urlPatterns);
    }

    public EnumSet<DispatcherType> getDispatcherTypes() {
        return dispatcherTypes;
    }

    public void setDispatcherTypes(EnumSet<DispatcherType> dispatcherTypes) {
        this.dispatcherTypes = dispatcherTypes;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
