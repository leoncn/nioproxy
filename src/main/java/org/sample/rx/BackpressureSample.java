package org.sample.rx;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by U0128754 on 1/25/2016.
 */
public class BackpressureSample {
    private static final Logger logger = LogManager.getLogger();

    private static void printSub(Observable obs, String prefix) {
        CountDownLatch latch = new CountDownLatch(1);
        obs.finallyDo(() -> latch.countDown())
                .subscribe(e -> logger.printf(Level.INFO, "%s > %s", prefix, e),
                        err -> logger.error(err),
                        ()-> logger.info("completed"));
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

//        logger.info("debounce");
//        printSub(data().timeInterval()
//                .doOnEach(logger::info)
//                .debounce(300, TimeUnit.MILLISECONDS), "throttle_first");
//
//
//        Observable
//                .range(0, 500_000_000).repeat()
//                .buffer(50, 50, TimeUnit.MILLISECONDS)
//              //  .onBackpressureDrop()
//                .zipWith(Observable.interval(1000, TimeUnit.MILLISECONDS), (x, y) -> x)
//
//                .observeOn(Schedulers.newThread())
//                .subscribe(
//                        i -> {},
//                        e -> logger.error("",e),
//                        () -> logger.info("Completed")
//                );


        //http://stackoverflow.com/questions/32930410/why-do-we-need-publish-and-refcount-rx-operators-in-this-case/32935647#32935647
//        Observable source = data().timestamp().publish().refCount();
//        printSub(source.buffer(source.debounce(300, TimeUnit.MILLISECONDS)),
//                "boundary"
//        );
//
//        printSub(source.buffer(source.debounce(300, TimeUnit.MILLISECONDS)),
//                "boundary2"
//        );

        Observable shared = data()
                //.publish().refCount();
                .publish( stream -> {
           return  stream;
        }).doOnEach(logger::info).cache();

        printSub(shared, "multicast 1");
        TimeUnit.SECONDS.sleep(1);
        printSub(shared, "multicast 1");
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
