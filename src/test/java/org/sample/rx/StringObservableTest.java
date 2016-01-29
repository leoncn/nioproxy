package org.sample.rx;

import org.junit.Test;

import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by U0128754 on 1/29/2016.
 */
public class StringObservableTest {

    @Test
    public void testByCharacters() throws Exception {
            int N = 1000;
        Random rand = new Random((N));
        String string = "abcd";
//                IntStream.range(0,1000)
//                .mapToObj( i -> rand.nextInt())
//                .map(num -> (char)( 'a' + num%('z' - 'a') + 1))
//                .collect(Collectors.joining());

    }
}