package org.sample.rx;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.observers.Subscribers;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by U0128754 on 1/21/2016.
 */
public class RetryWhenCase {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        Supplier<Subscriber> logSubFunc = () -> {
            return Subscribers.create(
                    logger::info,
                    e -> logger.error(e),
                    () -> logger.info("Completed.")
            );
        };

        Observable<Integer> simpleRetry = Observable.create(new ErrorEmitter())
                .retry(( attempts, error ) -> {
                    logger.printf(Level.ERROR, "%d retry due to %s.", attempts, error.getMessage());
                    return attempts < 3;
                });

//        simpleRetry.subscribe(logSubFunc.get());


        Func1<? super Observable<? extends java.lang.Throwable>, Observable> dummyEmit = (errorObs) -> {
            return errorObs.flatMap(error -> Observable.error(error));
        };

        Observable<Integer> dummyWhen = Observable.create(new ErrorEmitter())
                .retryWhen(errorEvent -> dummyEmit.call(errorEvent));
//                .retry((attempts, error) -> {
//                    logger.printf(Level.ERROR, "%d retry due to %s.", attempts, error.getMessage());
//                    return attempts < 3;
//                });
        dummyWhen.subscribe(logSubFunc.get());

        Func1<? super Observable<? extends java.lang.Throwable>, ? extends Observable<?>>
                delayOneSec = errorObs -> errorObs.flatMap(error ->
                     Observable.timer(1L , TimeUnit.SECONDS)
        );

        Observable<Integer> oneSecDelayThenRetry = Observable.create(new ErrorEmitter())
                .retryWhen(errorEvent -> delayOneSec.call(errorEvent));
//                .retry((attempts, error) -> {
//                    logger.printf(Level.ERROR, "%d retry due to %s.", attempts, error.getMessage());
//                    return attempts < 3;
//                });
       // oneSecDelayThenRetry.subscribe(logSubFunc.get());

        Thread.sleep(5 * 1000);
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

