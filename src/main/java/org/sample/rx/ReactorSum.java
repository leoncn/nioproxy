package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by U0128754 on 1/11/2016.
 */
public class ReactorSum {
    static Logger log = LogManager.getLogger();

    public static void main(String[] args) {

        Observable<String> lineInput = Observable.create((Subscriber<? super String> subscriber) -> {

            if (subscriber.isUnsubscribed())
                return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String line = null;

            try {
                while (!subscriber.isUnsubscribed() && (line = reader.readLine()) != null) {
                    subscriber.onNext(line);
                }
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
                //.publish();
//
       Observable o = Observable.create((Subscriber<? super String> subscriber) -> {
           if (subscriber.isUnsubscribed())
               return;
           BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

           String line = null;


           try {
               while (!subscriber.isUnsubscribed() && (line = reader.readLine()) != null) {
                   log.info(line);
                   subscriber.onNext(line);
               }
               subscriber.onCompleted();
           } catch (IOException e) {
               subscriber.onError(e);
           }
           subscriber.onCompleted();
               });

        o.subscribeOn(Schedulers.computation()).subscribe(log::info);
        o.subscribe(log::error);

//        Function<Pattern, Observable<Integer>> lineParser = (pattern) -> {
//            Observable<Integer> obs = lineInput.filter(line ->
//            {
//                System.out.println(pattern.toString());
//                return pattern.matcher(line).matches();
//            }
//
//            ).map(line ->
//                    {
//                      return Integer.parseInt(line.substring(line.indexOf("=") + 1).trim());
//                    }
//            );
//            return obs;
//        };
//
//        Observable.combineLatest(
//                lineParser.apply(Pattern.compile("^a.*")),
//                lineParser.apply(Pattern.compile("^b.*")),
//                (x, y) -> x + y
//        ).subscribe(
//                System.out::println
//        );

     //   lineInput.connect();
    }
}
