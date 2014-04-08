package graphvis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ VectorWritableTest.class,
		FruchtermanReingoldGraphVisTest.class, IntegrationTest.class, AggregatorTest.class })
public class AllTests {

}
