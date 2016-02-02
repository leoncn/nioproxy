package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observables.AbstractOnSubscribe;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by U0128754 on 1/19/2016.
 */
public class MapOperators {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {
//
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

//
//        AtomicInteger fails = new AtomicInteger();
//        int numFails = 50;
//
//
//        Observable.OnSubscribe ons = AbstractOnSubscribe.create(s -> {
//            long c = s.calls();
//            switch (s.phase()) {
//                case 0:
//                    s.onNext("Beginning " + fails.get());
//
//                    if (fails.getAndIncrement() == numFails) {
//                        s.advancePhase();
//                    } else {
//                        s.onError(new RuntimeException("Oh, failure."));
//                    }
//                    break;
//                case 1:
//                    s.onNext("Beginning!");
//                    s.advancePhase();
//                    break;
//                case 2:
//                    s.onNext("Finally working");
//                    s.onCompleted();
//                    s.advancePhase();
//                    break;
//                default:
//                    throw new IllegalStateException("How did we get here?" + s.phase());
//            }
//        });
//
//
//        AtomicBoolean done = new AtomicBoolean(false);
//        Subscriber sub = Subscribers.create(
//                logger::info,
//                e -> logger.error("", e),
//                () -> { done.set(true); logger.info("done");}
//        );
//
//
//        while(!done.get())
//          Observable.create(ons).subscribe(logger::info,
//                  e -> logger.error(e),
//                  () -> { done.set(true); logger.info("done");});
//
//
//        Thread.sleep(10000);
    }
}
