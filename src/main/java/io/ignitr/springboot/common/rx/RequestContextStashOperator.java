package io.ignitr.springboot.common.rx;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import rx.Observable;
import rx.Subscriber;

/**
 * Operator that handles setting the Spring request attributes from the constructing thread onto the
 * execution thread of an Observable.
 *
 * @param <T>
 */
public class RequestContextStashOperator<T> implements Observable.Operator<T, T> {
    private final RequestAttributes attributes;

    public RequestContextStashOperator() {
        attributes = RequestContextHolder.currentRequestAttributes();
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> subscriber) {
        return new Subscriber<T>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(T t) {
                RequestContextHolder.setRequestAttributes(attributes);
                subscriber.onNext(t);
            }
        };
    }
}
