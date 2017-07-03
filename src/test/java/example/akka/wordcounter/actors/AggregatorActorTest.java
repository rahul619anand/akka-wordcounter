package example.akka.wordcounter.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import example.akka.wordcounter.utils.Constants;
import org.junit.jupiter.api.*;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.stream.IntStream;

/**
 * Created by ranand on 7/2/2017 AD.
 */

@RunWith(JUnitPlatform.class)
public class AggregatorActorTest {

    static ActorSystem system;

    @BeforeAll
    static void initAll() {
        //setting up actor system
        system = ActorSystem.create();
    }


    @Test
    @DisplayName("AggregatorActor should correctly process events (START_OF_FILE,LINE,END_OF_FILE) in sequence under specified duration")
    void aggregateSequenceTest() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell(Constants.START_OF_FILE, getRef());
                expectMsg(duration("2 second"), "All set to process...sample.log");

                within(duration("5 seconds"), () -> {
                    aggregatorActor.tell("hello world", getRef());
                    aggregatorActor.tell(Constants.END_OF_FILE, getRef());

                    expectMsg(duration("2 second"), "sample.log : 2");
                    return null;
                });
            }
        };

    }

    @Test
    @RepeatedTest(10) // checking it 10 times to verify if calculation is consistent
    @DisplayName("AggregatorActor should correctly calculate word count when it receives multiple LINE's of a file concurrently")
    void wordCountCorrectnessCheck() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell(Constants.START_OF_FILE, getRef());
                expectMsg(duration("5 second"), "All set to process...sample.log");

                within(duration("10 seconds"), () -> {
                    // using parallel call to check concurrent consistency
                    IntStream.range(1, 50).parallel().forEach(i -> aggregatorActor.tell("hello " + i, getRef()));

                    aggregatorActor.tell(Constants.END_OF_FILE, getRef());

                    expectMsg(duration("5 second"), "sample.log : 98");
                    return null;
                });
            }
        };

    }


    @Test
    @DisplayName("AggregatorActor should return word count as 0 if it doesn't receives any LINE of a file to parse")
    void zeroWordCheck() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell(Constants.START_OF_FILE, getRef());
                expectMsg(duration("2 second"), "All set to process...sample.log");
                aggregatorActor.tell(Constants.END_OF_FILE, getRef());
                expectMsg(duration("2 second"), "sample.log : 0");

            }
        };

    }

    @Test
    @DisplayName("AggregatorActor should return error message if it receives LINE of a file before START_OF_FILE event for any file")
    void invalidLineMessage() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell("hello world", getRef());
                expectMsg(duration("2 second"), "Can't send LINE before START_OF_FILE");

            }
        };

    }

    @Test
    @DisplayName("AggregatorActor should return error message if it receives END_OF_FILE before START_OF_FILE event for any file")
    void invalidEndOfFileMessage() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell(Constants.END_OF_FILE, getRef());
                expectMsg(duration("2 second"), "Can't send END_OF_FILE before START_OF_FILE");

            }
        };

    }

    @Test
    @DisplayName("AggregatorActor should not return any message if it receives UNKNOWN EVENT")
    void invalidEventMessage() {
        new TestKit(system) {
            {
                final ActorRef aggregatorActor = system.actorOf(AggregatorActor.props("sample.log"));
                aggregatorActor.tell(new Object(), getRef());
                expectNoMsg(duration("1 second"));

            }
        };

    }

    @AfterAll
    static void tearDownAll() {
        //cleaning up actor system
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
