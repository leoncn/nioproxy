package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by U0128754 on 1/19/2016.
 */
public class MapOperators {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        Supplier<Subscriber> logSubFunc = () -> {
            return Subscribers.create(
                    logger::info,
                    e -> logger.error(e),
                    () -> logger.info("Completed.")
            );
        };

        Observable.just(1,2,3).flatMapIterable(i -> IntStream.range(0, i).collect(
                ArrayList::new,
                ArrayList::add,
                ArrayList::addAll
        )).subscribe(logSubFunc.get());

        Observable.just(1,2,3)
                .flatMap(i -> {
                   return Observable.range(0,i);
                })
                .subscribe(logSubFunc.get());


        logger.info("switchMap operator");
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(5)
                .switchMap(i -> {
                    logger.info("r " + i);
                    return Observable.interval(20, TimeUnit.MILLISECONDS)
                            .map(
                                    x ->String.format("Source emits %d, switchMap emits %d", i, x)
                            );

                })
                .subscribe(logSubFunc.get());

        Thread.sleep(1000);
    }
}
