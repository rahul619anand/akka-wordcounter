package example.akka.wordcounter.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import example.akka.wordcounter.utils.Constants;
import example.akka.wordcounter.utils.FileSystemUtils;
import org.junit.jupiter.api.*;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Created by ranand on 7/2/2017 AD.
 */

@RunWith(JUnitPlatform.class)
public class FileScannerActorTest {

    static ActorSystem system;

    TestKit probe;

    @BeforeAll
    static void initAll() {
        //setting up actor system
        system = ActorSystem.create();
    }

    @BeforeEach
    void init() {
        // probe is used for injection and later inspecting it for expected values
        probe = new TestKit(system);
    }

    @Test
    @DisplayName("FileScannerActor should send absolute file path of all files in the specified directory to FileParserActor")
    void fileSentCountCheck() {
        new TestKit(system) {
            {
                final ActorRef fileScannerActor = system.actorOf(FileScannerActor.props(probe.getRef(), new FileSystemUtils()));
                fileScannerActor.tell(Constants.SCAN, getRef());
                awaitCond(probe::msgAvailable); // awaiting until message is available to probe
                probe.receiveN(2); // expects no of files for parsing
            }
        };

    }

    @Test
    @DisplayName("FileScannerActor should send all files names to FileParserActor within a specified duration")
    void fileSentDurationCheck() {
        new TestKit(system) {
            {
                final ActorRef fileScannerActor = system.actorOf(FileScannerActor.props(probe.getRef(), new FileSystemUtils()));
                within(duration("5 seconds"), () -> {
                    fileScannerActor.tell(Constants.SCAN, getRef());
                    awaitCond(probe::msgAvailable); // awaiting until message is available to probe
                    probe.receiveN(2, duration("5 second")); // checking if message is available in some accepted duration
                    return null;
                });
            }
        };

    }

    @AfterEach
    void tearDown() {

    }

    @AfterAll
    static void tearDownAll() {
        // cleaning up actor system
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
