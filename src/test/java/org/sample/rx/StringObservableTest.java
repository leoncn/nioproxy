package org.sample.rx;

import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by U0128754 on 1/29/2016.
 */
public class StringObservableTest {

    @Test
    public void testByCharacters() throws Exception {
        int N = 1000;
        Random rand = new Random((N));

        IntStream.range(0, N).forEach( i -> {
            String input = IntStream.range(0, i)
                    .mapToObj(j -> rand.nextInt())
                    .map(num -> String.valueOf((char)('a' + num % ('z' - 'a' + 1))))
                    .collect(Collectors.joining());

            TestSubscriber subscriber = new TestSubscriber();

            StringObservable.byCharacters(Observable.just(input))
                    .scan( (x,y) -> x.concat(y))
                    .subscribe(subscriber);

            subscriber.assertNoErrors();
            subscriber.assertUnsubscribed();

            int numEvents = subscriber.getOnNextEvents().size();
            Assert.assertEquals(numEvents, i);
            if(numEvents > 0)
                Assert.assertEquals(subscriber.getOnNextEvents().get(numEvents-1), input);
        });
    }

    @Test
    public void testJoin() {
        int N = 1000;
        Random rand = new Random(Instant.now().getNano());

        IntStream.range(0, N).parallel().forEach(
                i -> {
                    final List<String> strs = IntStream.range(0, rand.nextInt(N))
                            .mapToObj(j->randNString(j))
                            .collect(Collectors.toList());

                    TestSubscriber subscriber = new TestSubscriber();
                    StringObservable
                            .join(Observable.from(strs), " ")
                            .subscribe(subscriber);

                    subscriber.assertNoErrors();
                    subscriber.assertUnsubscribed();
                    Assert.assertEquals(subscriber.getOnNextEvents().size(), 1);
                    Assert.assertEquals(
                            subscriber.getOnNextEvents().get(0),
                            strs.stream().collect(Collectors.joining(" ")));
                }
        );
    }


    @Test
    public void testConcat() {
        Random rand = new Random(Instant.now().getNano());
        int count = rand.nextInt(1000);
        ConnectableObservable<String> multicast = Observable
                .range(0,count)
                .map(i -> randNString(i)).publish();

        TestSubscriber subscriber = new TestSubscriber();
        StringObservable.concat(multicast).subscribe(subscriber);

        StringBuilder b = new StringBuilder();
        multicast.forEach(str -> b.append(str));

        multicast.connect();
        subscriber.assertNoErrors();
        subscriber.assertUnsubscribed();
        Assert.assertEquals(subscriber.getOnNextEvents().size(), count);

        if(count > 0)
        Assert.assertEquals(
                b.toString(), subscriber.getOnNextEvents().get(count - 1)
               );
    }

    private String randNString(int n) {
        Random rand = new Random(Instant.now().getNano());
        return rand.ints(0, 128)
                .mapToObj(i-> String.valueOf((char)i))
                .limit(n)
                .collect(Collectors.joining());
    }
}