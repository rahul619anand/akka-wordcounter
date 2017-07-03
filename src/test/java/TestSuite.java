import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

/**
 * Created by ranand on 7/2/2017 AD.
 */

//TODO : need to fix this ( till then please use "gradle test" )
/* There is some issue with running JUnit 5 Test Suite on intellij */
@RunWith(JUnitPlatform.class)
@SelectPackages("example.akka.wordcounter")
public class TestSuite {
}