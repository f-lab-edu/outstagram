package com.outstagram.outstagram.async;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;

public class ContextAwareCallable<V> implements Callable<V> {
    private final Callable<V> task;
    private final RequestAttributes requestAttributes;

    public ContextAwareCallable(Callable<V> task) {
        this.task = task;
        this.requestAttributes = RequestContextHolder.getRequestAttributes();
    }

    @Override
    public V call() throws Exception {
        RequestContextHolder.setRequestAttributes(requestAttributes);
        try {
            return task.call();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
