/*
 * Copyright 2016 Greg Whitaker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
