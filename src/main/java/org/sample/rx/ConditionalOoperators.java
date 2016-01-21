package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observers.Subscribers;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by U0128754 on 1/21/2016.
 */
public class ConditionalOoperators {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        Supplier<Subscriber> logSubFunc = () -> {
            return Subscribers.create(
                    logger::info,
                    e -> logger.error(e),
                    () -> logger.info("Completed.")
            );
        };


        Observable.amb(
                Observable.range(0, 10)
                        .doOnNext(i-> i*=10)
                   //     .delay(100, TimeUnit.MILLISECONDS)
                ,
                Observable.range(10, 10)
                        .doOnNext(i -> logger.info("2nd"))
                        .delay(50L, TimeUnit.MILLISECONDS)
        )
                .subscribe(logSubFunc.get());


        Observable<String> words = Observable // (1)
                .just("one", "way", "or", "another", "I'll", "learn", "RxJava")
                .zipWith(
                        Observable.interval(200L, TimeUnit.MILLISECONDS),
                        (x, y) -> x
                );
        words.takeWhile(w -> w.length() > 2).subscribe(logSubFunc.get());

        words
                .skipUntil(Observable.interval(1000, TimeUnit.MILLISECONDS))
                .subscribe(logSubFunc.get());

//        Observable.empty().defaultIfEmpty("Hello").subscribe(logSubFunc.get());


        Thread.sleep(10000);
    }
}
