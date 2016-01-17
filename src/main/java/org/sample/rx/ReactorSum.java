package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.observers.SafeSubscriber;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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

                if (!subscriber.isUnsubscribed()) {

                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });

        ConnectableObservable<String> notEmitUntilConn = lineInput.publish();


        Function<Pattern, Observable<Integer>> lineParser = (pattern) ->
                notEmitUntilConn
                        .filter(line ->
                                {
                                    return pattern.matcher(line).matches();
                                }
                        )
                        .map(line ->
                                {
                                    return Integer.parseInt(line.substring(line.indexOf("=") + 1).trim());
                                }
                        );

        Subscriber<Integer> sub = new SafeSubscriber<Integer>(new Subscriber<Integer>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(Integer s) {
                System.out.println(s);
            }
        });

        sub.add(Subscriptions.create( () -> {
            System.out.println("unsubscribe callback");
        }));


        Observable.combineLatest(
                lineParser.apply(Pattern.compile("^a.*")),
                lineParser.apply(Pattern.compile("^b.*")),
                (x, y) -> x + y
        ).subscribe(sub);


        notEmitUntilConn.connect();
    }
}
