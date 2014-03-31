package graphvis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CoordinatesWritableTest.class,
		FruchtermanReingoldGraphVisTest.class, IntegrationTest.class })
public class AllTests {

}
