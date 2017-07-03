package example.akka.wordcounter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ranand on 7/3/2017 AD.
 */

/**
 * Refer this ( https://www.bountysource.com/issues/38171780-org-springframework-boot-loader-launchedurlclassloader-unable-to-load-3rd-party-filesystemprovider-implementation-from-spring-boot-executable-jar )
 * to understand why I need to create file system in first place.
 * <p>
 * Although this is a spring boot executable jar issue, it behaves similarly for any other executable jar.
 */
public class FileSystemUtils {
    private static final Logger log = LoggerFactory.getLogger(FileSystemUtils.class);


    FileSystem fileSystem;

    /**
     * initializes file system
     *
     * @param uri uri for creating filesystem
     * @return
     * @throws IOException
     */
    public FileSystem initFileSystem(URI uri) throws IOException {
        try {
            if (fileSystem == null) {
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");

                //only create if running in executable jar
                if (uri.toString().startsWith("jar:")) {
                    fileSystem = FileSystems.newFileSystem(uri, env, getClass().getClassLoader());
                }

            } else {
                fileSystem = FileSystems.getFileSystem(uri);
            }
        } catch (FileSystemNotFoundException e) {
            log.error("FileSystemNotFoundException : {}", e);
        }
        return fileSystem;
    }

    // cleans up filesystem
    public void close() {
        log.debug("Closing fileSystem");
        try {
            if (fileSystem != null) {
                fileSystem.close();
            }
        } catch (IOException e) {
            log.error("Exception in Closing fileSystem ", e);
        }
    }

}
