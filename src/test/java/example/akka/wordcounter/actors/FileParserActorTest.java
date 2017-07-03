package example.akka.wordcounter.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ranand on 7/2/2017 AD.
 */

@RunWith(JUnitPlatform.class)

public class FileParserActorTest {

    static ActorSystem system;

    @BeforeAll
    static void initAll() {
        //setting up actor system
        system = ActorSystem.create();
    }


    @Test
    @DisplayName("FileParserActor should correctly parse all files by sending different events (START_OF_FILE,LINE,END_OF_FILE) in sequence")
    void fileParserCheck() {
        new TestKit(system) {{
            ActorRef fileParserActor = system.actorOf(FileParserActor.props());
            URL url = ClassLoader.getSystemResource("log");
            if (url != null) {
                try {
                    Files.walk(Paths.get(url.toURI()))
                            .filter(Files::isRegularFile)
                            .forEach(file -> {
                                fileParserActor.tell(file, getRef());
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            receiveN(2); // returns no of files it parsed
        }};
    }

    @Test
    @DisplayName("FileParserActor should not return any message if it receives an unknown event")
    void unknownEventCheck() {
        new TestKit(system) {{
            ActorRef fileParserActor = system.actorOf(FileParserActor.props());
            fileParserActor.tell("test.log", getRef());
            expectNoMsg(duration("1 second")); // sending unknown events should lead to no response
        }};
    }

    @Test
    @DisplayName("FileParserActor should return proper error message if file not found")
    void fileNotFoundCheck() {
        new TestKit(system) {{
            ActorRef fileParserActor = system.actorOf(FileParserActor.props(), "fileparser");
            assertEquals("default", system.name());
            fileParserActor.tell(Paths.get("test.log"), getRef());
            expectMsg("NoSuchFileException");


        }};
    }


    @AfterAll
    static void tearDownAll() {
        //cleaning up actor system
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
