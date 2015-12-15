import org.apache.logging.log4j.Level;
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SimpleDeferred def = new SimpleDeferred();
        CompletableFuture<Optional<String>> thisFuture = CompletableFuture.supplyAsync(() -> def.doThis());
        CompletableFuture<Optional<String>> thatFuture = CompletableFuture.supplyAsync(() -> def.doThat());

        thisFuture
                .thenCombineAsync(thatFuture, def::joinOptionalString)
                .thenAcceptAsync((opts) -> def.logger.printf(Level.INFO, "%s", opts.get())).join();


    }

    private <T> T longTimeOPeration(Supplier<T> supplier) {
        return supplier.get();
    }

    private Optional<String> doThis() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("in THIS ");
        return Optional.of(this.longTimeOPeration(() -> "I complete this work."));
    }

    private Optional<String> doThat() {
        logger.info("in that ");
        return Optional.of(this.longTimeOPeration(() -> "I complete that work."));
    }

    private Optional<String> joinOptionalString(Optional<String> opt, Optional<String> otherOpt) {
        logger.info("joining");
        return opt.isPresent() ?
                (otherOpt.isPresent() ? Optional.of(opt.get().concat(otherOpt.get())) : opt)
                : otherOpt;
    }

}
