package org.sample.rx;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Notification;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Created by U0128754 on 1/21/2016.
 */
public class RetryWhenCase {
    private static final Logger logger = LogManager.getLogger();

    private static <T> Action1<Notification<? super T>> debug(String desc) {

        Function<String, Action1<Notification<? super T>>> debugger = (prefix) -> {

            AtomicReference<String> nextOffset = new AtomicReference<String>(">");
            return (Notification<? super T> notif) -> {
                if (notif == null) {
                    logger.error("null notification.");
                }

                switch (notif.getKind()) {
                    case OnNext: {
                        logger.printf(Level.INFO, "%s %s %s", prefix, nextOffset.get(), notif.getValue());
                        nextOffset.getAndUpdate(str -> str + "-");
                        break;
                    }
                    case OnError: {
                        logger.printf(Level.ERROR, "%s %s", prefix, notif.getThrowable().getMessage());
                        break;
                    }
                    case OnCompleted: {
                        logger.printf(Level.ERROR, "%s %s", prefix, "completed");
                        break;
                    }
                    default:
                        logger.error("Unsupport operation");
                }
            };
        };
        return debugger.apply(desc);
    }

    public static void main(String[] args) throws InterruptedException {


        Function<String, Subscriber> logSubFunc = (prefix) -> {
            return Subscribers.create(
                    e -> logger.printf(Level.INFO, "%s : %s", prefix, e),
                    e -> logger.error(prefix, e),
                    () -> logger.info(prefix + " Completed.")
            );
        };


        Observable.interval(100L,TimeUnit.MILLISECONDS, Schedulers.immediate()).take(5).doOnEach(debug("100 ms")).subscribe();

        Thread.sleep(1000);
        Observable<Integer> simpleRetry = Observable.create(new ErrorEmitter())
                .doOnEach(debug("simple retry"))
                .retry(3);

        simpleRetry.subscribe(
                //logSubFunc.apply("simple retry")
                 );

//
//        Func1<? super Observable<? extends java.lang.Throwable>, Observable> emitError = (errorObs) -> {
//            return errorObs.flatMap(error -> Observable.error(error));
//        };
//
//        Observable<Integer> dummyWhen = Observable.create(new ErrorEmitter())
//                .retryWhen(errorEvent -> emitError.call(errorEvent))
//                .retry((attempts, error) -> {
//                    logger.printf(Level.ERROR, "%d retry due to %s.", attempts, error.getMessage());
//                    return attempts < 3;
//                });
//        dummyWhen.subscribe(logSubFunc.apply("emit error retry"));
//
//        Func1<? super Observable<? extends java.lang.Throwable>, ? extends Observable<?>>
//                delayOneSec = errorObs -> errorObs.flatMap(error ->
//                Observable.timer(1L, TimeUnit.SECONDS)
//        );
//
//        Observable<Integer> oneSecDelayThenRetry = Observable.create(new ErrorEmitter())
//                .retryWhen(errorEvent -> delayOneSec.call(errorEvent));
//        oneSecDelayThenRetry.subscribe(logSubFunc.apply("one sec delay"));

        Thread.sleep(10 * 1000);
    }


}

class FooException extends RuntimeException {
    public FooException() {
        super("Foo!");
    }
}

class BooException extends RuntimeException {
    public BooException() {
        super("Boo!");
    }
}

class ErrorEmitter implements OnSubscribe<Integer> {
    private int throwAnErrorCounter = 5;

    @Override
    public void call(Subscriber<? super Integer> subscriber) {
        subscriber.onNext(1);
        subscriber.onNext(2);

        if (throwAnErrorCounter > 4) {
            throwAnErrorCounter--;
            subscriber.onError(new FooException());
            return;
        }
        if (throwAnErrorCounter > 0) {
            throwAnErrorCounter--;
            subscriber.onError(new BooException());
            return;
        }
        subscriber.onNext(3);
        subscriber.onNext(4);
        subscriber.onCompleted();
    }
}

