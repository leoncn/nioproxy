package org.sample.rx;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.observers.Subscribers;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by U0128754 on 1/29/2016.
 */
public class StringObservable {

    public static Observable<String> byCharacters(Observable<String> source) {
            return source.lift(new Operator<String, String>() {
                @Override
                public Subscriber<? super String> call(final Subscriber<? super String> subscriber) {
                    return Subscribers.create(
                            str -> {
                                for(char ch : str.toCharArray())
                                    subscriber.onNext(String.valueOf(ch));
                            },
                            subscriber::onError,
                            subscriber::onCompleted
                    );
                }
            });
    };
}
