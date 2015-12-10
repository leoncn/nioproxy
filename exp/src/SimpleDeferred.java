import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Created by U0128754 on 12/10/2015.
 */
public class SimpleDeferred {

    Logger logger = LogManager.getLogger();

    private <T> T longTimeOPeration(Supplier<T> supplier) {
        return supplier.get();
    }

    private Optional<String> doThis() {
       return Optional.of(this.longTimeOPeration(() -> "I complete this work."));
    }

    private Optional<String> doThat() {
        return  Optional.of(this.longTimeOPeration(() -> "I complete that work."));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SimpleDeferred def = new SimpleDeferred();
        CompletableFuture<Optional<String>> thisFuture =  CompletableFuture.supplyAsync(() -> def.doThis());
        CompletableFuture<Optional<String>> thatFuture =  CompletableFuture.supplyAsync(() -> def.doThat());

       thisFuture.thenCombine(
               thatFuture, (p1, p2) ->CompletableFuture.supplyAsync(() -> p1.orElse(p2.get()))).get().get();

    }

}
