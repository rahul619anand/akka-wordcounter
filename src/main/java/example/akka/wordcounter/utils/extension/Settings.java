package example.akka.wordcounter.utils.extension;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.ExtensionIdProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ranand on 7/1/2017 AD.
 */

public class Settings extends AbstractExtensionId<SettingsImpl>
        implements ExtensionIdProvider {

    private final Logger log = LoggerFactory.getLogger(Settings.class);

    public final static Settings SETTINGS = new Settings();

    // private constructor to block object creation
    private Settings() {
    }

    // lookup() is called by ExtensionIdProvider to load extension at ActorSystem startup
    public Settings lookup() {
        log.debug("Lookup called to load extension");
        return Settings.SETTINGS;
    }

    // called by Akka to instantiate SettingsImpl
    public SettingsImpl createExtension(ExtendedActorSystem system) {
        log.debug("SettingsImpl instantiated");
        // akka loads the config while creating actor system
        return new SettingsImpl(system.settings().config());
    }
}