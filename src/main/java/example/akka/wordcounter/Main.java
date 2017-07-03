package example.akka.wordcounter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import example.akka.wordcounter.actors.FileParserActor;
import example.akka.wordcounter.actors.FileScannerActor;
import example.akka.wordcounter.utils.Constants;
import example.akka.wordcounter.utils.FileSystemUtils;
import example.akka.wordcounter.utils.extension.SettingsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static example.akka.wordcounter.utils.extension.Settings.SETTINGS;

/**
 * Created by ranand on 7/1/2017 AD.
 */

/**
 * Main is the place where it all starts.
 * This is the class invoked, when the executable jar is run.
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Creating Actor System ...");
        final ActorSystem system = ActorSystem.create("custom-actor-system");
        FileSystemUtils fileSystemUtils = new FileSystemUtils();

        try {
            // Creating FileParserActor actor
            final ActorRef fileParserActor =
                    system.actorOf(FileParserActor.props(), "FileParserActor");

            // Creating FileScannerActor actor with FileParserActor injected to it.
            final ActorRef fileScannerActor =
                    system.actorOf(FileScannerActor.props(fileParserActor, new FileSystemUtils()), "FileScannerActor");

            //disabled by default, enable from config
            if (getSettings(system).getExecutionMode().equals("scheduler")) {
                int interval = getSettings(system).getExecutionInterval();
                log.debug("Running SCHEDULED SCAN to calculate word count every {} second, ", interval);
                system.scheduler().schedule(
                        Duration.create(5, TimeUnit.MILLISECONDS),
                        Duration.create(interval, TimeUnit.SECONDS)
                        , fileScannerActor
                        , Constants.SCAN, system.dispatcher(), ActorRef.noSender());
            } else {
                log.debug("Running single SCAN to calculate word count");
                // invoking FileScannerActor to start SCAN process
                fileScannerActor.tell(Constants.SCAN, fileParserActor);
            }

        } catch (Exception e) {
            log.error("Exception: ", e);
            fileSystemUtils.close();
            system.terminate();
        }
    }

    /**
     * Gets SettingsExtension instance on the speified actor-system
     *
     * @param system actor-system
     * @return
     */
    private static SettingsImpl getSettings(ActorSystem system) {
        return SETTINGS.get(system);
    }
}
