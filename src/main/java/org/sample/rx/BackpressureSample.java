package org.sample.rx;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by U0128754 on 1/25/2016.
 */
public class BackpressureSample {
    private static final Logger logger = LogManager.getLogger();

    private static void printSub(Observable obs, String prefix) {
        CountDownLatch latch = new CountDownLatch(1);
        obs.finallyDo(() -> latch.countDown())
                .subscribe(e -> logger.printf(Level.INFO, "%s > %s", prefix, e));
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws InterruptedException {
//        logger.info("Sampling");
//        printSub(data().timeInterval().doOnEach(logger::info).sample(1000, TimeUnit.MILLISECONDS), "sampling");
//
//        logger.info("throttle_last is the samle as sample");
//        printSub(data().timeInterval().doOnEach(logger::info).throttleLast(1000, TimeUnit.MILLISECONDS), "throttle_last");
//
//        logger.info("throttle_first");
//        printSub(data().timeInterval().doOnEach(logger::info).throttleFirst(1000, TimeUnit.MILLISECONDS), "throttle_first");

        logger.info("debounce");
        printSub(data().timeInterval()
                .doOnEach(logger::info)
                .debounce(300, TimeUnit.MILLISECONDS), "throttle_first");
    }

    private static Observable<Integer> data() {

        return Observable.range(0, 1024)
                .zipWith(
                        Observable.interval(200, TimeUnit.MILLISECONDS)
                                .take(4)
                                .concatWith(Observable.interval(500, TimeUnit.MILLISECONDS).first()).repeat(5)
                        ,
                        (x, y) -> x
                );
    }
}
