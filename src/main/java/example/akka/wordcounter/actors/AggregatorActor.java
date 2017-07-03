package example.akka.wordcounter.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.wordcounter.utils.Constants;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by ranand on 7/1/2017 AD.
 */

/**
 * Aggregates the word count in a File and log to console.
 */
public class AggregatorActor extends AbstractActor {
    /**
     * Create Props for an actor of this type.
     *
     * @param filePath absolute path of the file , this is to be used while printing word-count.
     * @return a Props for creating this actor, which can then be further configured
     * (e.g. calling `.withDispatcher()` on it)
     */
    public static Props props(String filePath) {
        return Props.create(AggregatorActor.class, () -> new AggregatorActor(filePath));
    }

    /* Reasoning behind not using BigInteger for word count :
       Long.MAX_VALUE is 9,223,372,036,854,775,807
    */
    private AtomicLong counter; // thread safe counter
    private String filePath;

    public AggregatorActor(String filePath) {
        this.filePath = filePath;
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * this is where the messages received by this Actor lands.
     */
    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .matchEquals(Constants.START_OF_FILE, event -> {
                    initializeCounter();
                })
                .matchEquals(Constants.END_OF_FILE, event -> {
                    printWordCount();

                })
                .match(String.class, line -> {
                    parseLines(line);
                })
                .matchAny(event -> {
                    log.debug("{} : {}", Constants.UNKNOWN_EVENT, event);
                })
                .build();
    }

    /**
     * Intantiates a new counter for each file
     */
    private void initializeCounter() {
        log.debug("Received event: {}", Constants.START_OF_FILE);
        counter = new AtomicLong(0);
        sendEvent(getSender(), "All set to process..." + filePath);
    }

    /**
     * prints the word count of a file
     */
    private void printWordCount() {
        log.debug("Received event: {}", Constants.END_OF_FILE);
        if (counter != null) {
            log.info("{} , Word Count: {}", filePath, counter.get());
            sendEvent(getSender(), filePath + " : " + counter.get());
        } else {
            log.error("Can't send END_OF_FILE before START_OF_FILE");
            sendEvent(getSender(), "Can't send END_OF_FILE before START_OF_FILE");
        }
    }

    /**
     * Lines of a file are parsed and the word count of the respective file is increased
     *
     * @param line line of a file
     */
    private void parseLines(String line) {
        log.debug("Received LINE: {}", line);
        if (counter != null) {
            // TODO : would be better to use Guava Splitter here.
            Arrays.stream(line.trim()
                    .split(" "))   // split line by " "
                    .parallel()          // parallel stream
                    .filter(word -> !word.isEmpty())
                    .forEach(word -> {
                        counter.incrementAndGet(); // increment word counter
                    });
        } else {
            log.error("Can't send LINE before START_OF_FILE");
            sendEvent(getSender(), "Can't send LINE before START_OF_FILE");
        }
    }

    /**
     * Sends messages to actor
     *
     * @param actorRef ref of actor to send msg to.
     * @param message  message to be sent
     */
    private void sendEvent(ActorRef actorRef, String message) {
        actorRef.tell(message, getSelf());
    }
}

