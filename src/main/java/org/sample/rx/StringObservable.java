package org.sample.rx;

import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.observers.Subscribers;

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
                            for (char ch : str.toCharArray())
                                subscriber.onNext(String.valueOf(ch));
                        },
                        subscriber::onError,
                        subscriber::onCompleted
                );
            }
        });
    }

    public static Observable<String> concat(Observable<String> source) {
        return source.scan((s1, s2) -> s1.concat(s2));
    }

    public static Observable<String> join(Observable<String> source, final CharSequence separator) {
        return source.lift(child -> new JoinParentSubscriber(child, separator));
    }

    private static class JoinParentSubscriber extends Subscriber<String> {

        StringBuilder b = new StringBuilder();
        Subscriber<? super String> child = null;
        private CharSequence separator = " ";
        private boolean mayAddSeparator = false;

        public JoinParentSubscriber(Subscriber<? super String> child, CharSequence separator) {
            super(child);
            this.child = child;
            this.separator = separator;
        }

        @Override
        public void onCompleted() {
            if (!this.isUnsubscribed()) {
                child.onNext(b.toString());
            }

            if (!this.isUnsubscribed()) {
                child.onCompleted();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            if (!this.isUnsubscribed()) {
                child.onError(throwable);
            }
        }

        @Override
        public void onNext(String s) {
            if (this.mayAddSeparator) {
                b.append(this.separator);
            }

            b.append(s);
            this.mayAddSeparator = true;
        }
    }

}
