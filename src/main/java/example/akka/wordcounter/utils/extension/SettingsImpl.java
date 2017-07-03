package example.akka.wordcounter.utils.extension;

import akka.actor.Extension;
import com.typesafe.config.Config;

/**
 * Created by ranand on 7/1/2017 AD.
 */

/**
 * Extension are a kind of a singleton in an ActorSystem rather than a global singleton
 * Here it is used to just have application config
 */

public class SettingsImpl implements Extension {

    private Config config;

    /**
     * @param config typesafe config with root reference
     *               <p>
     *               akka already has typesafe config dependency and has defaults in reference.conf
     */
    SettingsImpl(Config config) {
        this.config = config;
    }


    // returns the log directory to be scanned
    public String getLogDirectory() {
        return config.getString("application.log-directory");
    }

    // returns the execution mode
    public String getExecutionMode() {
        return config.getString("application.execution-mode");
    }

    // returns the execution mode
    public Integer getExecutionInterval() {
        return config.getInt("application.scheduled-interval-in-seconds");
    }


}
