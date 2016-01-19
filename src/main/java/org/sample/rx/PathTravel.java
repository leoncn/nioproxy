package org.sample.rx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by U0128754 on 1/18/2016.
 */
public class PathTravel {

    private static final Logger logger = LogManager.getLogger();
    private static final String glob = "*";
    private static Func1<Path, Observable<String>> printFileFunc = (path) -> {

        if (path.toFile().isDirectory()) {
            return Observable.empty();
        }
        return Observable.create((Subscriber<? super String> subscriber) -> {
            if (subscriber.isUnsubscribed()) {
                return;
            }

            try {
                BufferedReader reader = Files.newBufferedReader(path);

                logger.info(padding("Open " + path, "-", 40));
                subscriber.add(Subscriptions.create(() -> {
                    try {
                        reader.close();
                        logger.info(padding("Close " + path, "-", 40));

                    } catch (IOException e) {
                        logger.error(e);
                    }
                }));

                String line = null;

                while (!subscriber.isUnsubscribed() && (line = reader.readLine()) != null) {
                    subscriber.onNext(line);
                }

            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                } else {
                    logger.error(e);
                }
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }).take(10);
    };
    private Func1<Path, Observable<Path>> listSubPath = (path) -> {
        Observable<Path> travelDir = Observable.create(subscriber -> {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, glob)) {
                stream.forEach((p) -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(p);
                    }
                });

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            } catch (IOException e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                } else {
                    logger.error(e);
                }
            }
        });

        return travelDir;
    };

    public static void main(String[] args) {
        List<String> dirs = Arrays.asList(".", "./no_exist");

        new PathTravel().travel(dirs)
                .flatMap(printFileFunc)
                .subscribe(
                        logger::info,
                        e -> logger.error(e),
                        () -> logger.info("done")
                );
    }

    private static String padding(String text, String dash, int len) {
        int textLen = text.length();

        if(len < textLen) return text;

        int left = (len - textLen)>>1, right = len-textLen-left;

        StringBuilder builder = new StringBuilder();

        IntStream.range(0, left).forEach(i -> builder.append(dash));
        builder.append(text);
        IntStream.range(0, right).forEach(i -> builder.append(dash));

        return builder.toString();
    }

    private Observable<Path> travel(Iterable<String> dirs) {
        return Observable.from(dirs)
                .map(dir -> Paths.get(dir))
                .filter((path) -> path.toFile().exists())
                .flatMap(listSubPath);
    }
}
