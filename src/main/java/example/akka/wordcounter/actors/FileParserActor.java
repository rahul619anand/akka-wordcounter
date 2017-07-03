package example.akka.wordcounter.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.wordcounter.utils.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by ranand on 7/1/2017 AD.
 */

/**
 * Sends START_OF_FILE event , lines of the file , END_OF_FILE event to AggregatorActor in same order
 * <p>
 * The order in which the events are delivered is maintained due to the queue used by mailbox of the actor
 * (i.e. FIFO)
 */
public class FileParserActor extends AbstractActor {
    /**
     * Create Props for an actor of this type.
     *
     * @return a Props for creating this actor, which can then be further configured
     * (e.g. calling `.withDispatcher()` on it)
     */
    public static Props props() {
        return Props.create(FileParserActor.class, () -> new FileParserActor());
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * this is where the messages received by this Actor lands.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Path.class, path -> {
                    /**
                     * a new instance of AggregatorActor is created per log file to manage individual word counts since requests for multiple files are processed in parallel
                     */
                    ActorRef actorRef = getContext().actorOf(AggregatorActor.props(path.toString()), "AggregatorActor" + UUID.randomUUID());

                    parseFileAndSendEventsForWordCount(path, actorRef);

                    getSender().tell("File :" + path, getSelf());

                })
                .matchAny(event -> {
                    // any unknown event is just logged and eaten-up
                    log.debug("{} : {}", Constants.UNKNOWN_EVENT, event);
                })
                .build();
    }

    /**
     * Parses File and send lines for aggregation
     *
     * @param path     absolute path of file to be parsed
     * @param actorRef ref of actor to send msg to.
     */
    private void parseFileAndSendEventsForWordCount(Path path, ActorRef actorRef) {
        log.debug("Request file path: {}", path);

        try (Stream<String> stream = Files.lines(path)) { //try-with-resources block

            // Sequence of Messages to AggregatorActor

            // 1. START_OF_FILE
            sendEvent(actorRef, Constants.START_OF_FILE);

            // 2. LINES of a file are sent in parallel to AggregatorActor
            stream.parallel()
                    .forEach(line -> {
                        log.debug("Line Read: " + line);
                        if (!line.isEmpty()) {
                            sendEvent(actorRef, line);
                        }

                    });

            // 3. END_OF_FILE
            sendEvent(actorRef, Constants.END_OF_FILE);

        } catch (NoSuchFileException e) {
            log.error("NoSuchFileException : {}",e);
            getSender().tell(e.getClass().getSimpleName(), getSelf());
        } catch (IOException e) {
            log.error("IOException : {}",e);
            getSender().tell(e, getSelf());
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

