package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observers.Subscribers;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by U0128754 on 1/20/2016.
 */
public class GroupOperators {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InterruptedException {

        Supplier<Subscriber> logSubFunc = () -> {
            return Subscribers.create(
                    logger::info,
                    e -> logger.error(e),
                    () -> logger.info("Completed.")
            );
        };

        Observable.interval(50, TimeUnit.MILLISECONDS).map(i -> 1 << i)
                .groupBy(i -> String.valueOf(i).length())
                .subscribe(grpObs -> {
                            logger.info("key: " + grpObs.getKey());
                          //  grpObs.timeInterval().subscribe(logSubFunc.get());
                        }
                );

        Thread.sleep(1000);

        Observable.just(1,3,5,5,7).distinctUntilChanged().subscribe(logSubFunc.get());
        Observable.just(1,3,5,5,7).distinctUntilChanged(i->i%2).subscribe(logSubFunc.get());
    }
}
