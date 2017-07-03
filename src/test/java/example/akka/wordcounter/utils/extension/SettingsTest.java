package example.akka.wordcounter.utils.extension;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static example.akka.wordcounter.utils.extension.Settings.SETTINGS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by ranand on 7/3/2017 AD.
 */
@RunWith(JUnitPlatform.class)
public class SettingsTest {

    static ActorSystem system;

    @BeforeAll
    static void initAll() {
        //setting up actor system
        system = ActorSystem.create();
    }

    @Test
    @DisplayName("Settings Extension should load all config correctly")
    void settingsCheck() {
        new TestKit(system) {
            {
                SettingsImpl settings = SETTINGS.get(system);
                assertAll("address",
                        () -> assertEquals("single", settings.getExecutionMode()),
                        () -> assertEquals(new Integer(30), settings.getExecutionInterval()),
                        () -> assertEquals("log", settings.getLogDirectory())
                );

            }
        };

    }

    @AfterAll
    static void tearDownAll() {
        // cleaning up actor system
        TestKit.shutdownActorSystem(system);
        system = null;
    }

}
