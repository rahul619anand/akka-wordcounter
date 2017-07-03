package example.akka.wordcounter.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import example.akka.wordcounter.utils.Constants;
import example.akka.wordcounter.utils.FileSystemUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static example.akka.wordcounter.utils.extension.Settings.SETTINGS;

/**
 * Created by ranand on 7/1/2017 AD.
 */

/**
 * Scans the log directory ( set in application.conf ) and sends all files absolute path to FileParserActor
 */
public class FileScannerActor extends AbstractActor {
    /**
     * Create Props for an actor of this type.
     *
     * @param actorRef reference to the FileParserActor
     * @return a Props for creating this actor, which can then be further configured
     * (e.g. calling `.withDispatcher()`  or `.withMailbox()` on it)
     */
    public static Props props(ActorRef actorRef, FileSystemUtils fileSystemUtils) {
        return Props.create(FileScannerActor.class, () -> new FileScannerActor(actorRef, fileSystemUtils));
    }

    private ActorRef actorRef;

    private FileSystemUtils fileSystemUtils;

    public FileScannerActor(ActorRef actorRef, FileSystemUtils fileSystemUtils) {
        this.actorRef = actorRef;
        this.fileSystemUtils = fileSystemUtils;
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * this is where the messages received by this Actor lands.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(Constants.SCAN, event -> {
                    log.debug("Request Received: {}", event);
                    try {
                        URL url = getUrl();
                        log.info("Scanning Directory: {}", url);

                        if (url != null) {
                            URI uri = url.toURI();

                            initilizeFileSystem(uri);

                            sendFilesForParsing(uri);

                            log.debug("Sent Files for parsing");
                        } else {
                            log.error("Directory could not be found");
                        }
                    } catch (IOException e) {
                        log.error("IOException : {}", e);
                        getSender().tell(e, getSelf());
                    }

                })
                .matchAny(event -> {
                    // any unknown event is just logged and eaten-up
                    log.debug("{} : {}", Constants.UNKNOWN_EVENT, event);
                })
                .build();
    }

    /**
     * Parses directory and sends all files (i.e. absolute paths) for parsing to FileParserActor
     *
     * @param uri uri of directory
     * @throws IOException
     */
    private void sendFilesForParsing(URI uri) throws IOException {

        Files.walk(Paths.get(uri))
                .filter(Files::isRegularFile) // check if it is not a directory
                .parallel() // requests are sent in parallel (this executes on multiple processor cores)
                .forEach(file -> {
                    log.debug("Sending File: {} for parsing", file.toString());
                    // sending absolute path of file for parsing to FileParserActor
                    actorRef.tell(file, getSelf());
                });
    }

    /**
     * @return url of the loaded directory
     */
    private URL getUrl() {
        // Settings Extension is called to get the log-directory to SCAN
        // log-directory should be present within src/main/resources

        String logDirectory = SETTINGS.get(getContext().getSystem()).getLogDirectory();
        return ClassLoader.getSystemResource(logDirectory);
    }

    /**
     * initilaizes file-system
     *
     * @param uri uri of the directory
     * @throws IOException
     */
    private void initilizeFileSystem(URI uri) throws IOException {
        log.debug("Parsing URI: {}", uri);
        fileSystemUtils.initFileSystem(uri);
    }


}

