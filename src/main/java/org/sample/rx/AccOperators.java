package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observers.Subscribers;

import java.util.function.Supplier;

/**
 * Created by U0128754 on 1/20/2016.
 */
public class AccOperators {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        Supplier<Subscriber> logSubFunc = () -> {
            return Subscribers.create(
                    logger::info,
                    e -> logger.error(e),
                    () -> logger.info("Completed.")
            );
        };

        Observable.range(1,10).scan(
                (x, y) -> x+y
        ).subscribe(logSubFunc.get());

        Observable.range(1,10).scan(
                (x, y) -> x+y
        ).last().subscribe(logSubFunc.get());
    }
}
