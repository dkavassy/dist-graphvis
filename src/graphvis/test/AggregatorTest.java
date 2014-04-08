package graphvis.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import graphvis.engine.GraphvisMasterCompute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.giraph.BspCase;
import org.apache.giraph.aggregators.AggregatorWrapper;
import org.apache.giraph.aggregators.DoubleSumAggregator;
import org.apache.giraph.aggregators.TextAggregatorWriter;
import org.apache.giraph.conf.ImmutableClassesGiraphConfiguration;
import org.apache.giraph.master.MasterAggregatorHandler;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.Progressable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AggregatorTest extends BspCase {


	public AggregatorTest() {
		super(AggregatorTest.class.getName());
		// TODO Auto-generated constructor stub
	}

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() throws IOException {
		super.tearDown();
	}
	
	private Map<String, AggregatorWrapper<Writable>> getAggregatorMap
    (MasterAggregatorHandler aggregatorHandler) {
	  try {
	    Field aggregtorMapField = aggregatorHandler.getClass().getDeclaredField
	        ("aggregatorMap");
	    aggregtorMapField.setAccessible(true);
	    return (Map<String, AggregatorWrapper<Writable>>)
	        aggregtorMapField.get(aggregatorHandler);
	  } catch (IllegalAccessException e) {
	    throw new IllegalStateException(e);
	  } catch (NoSuchFieldException e) {
	    throw new IllegalStateException(e);
	  }
}

	//test aggregators
	  /** Test if aggregators serialization captures everything */
	  @Test
	  public void testMasterAggregatorsSerialization() throws
	      IllegalAccessException, InstantiationException, IOException {
	   ImmutableClassesGiraphConfiguration conf = mock(ImmutableClassesGiraphConfiguration.class);
	    when(conf.getAggregatorWriterClass()).thenReturn(TextAggregatorWriter.class);
	    Progressable progressable = mock(Progressable.class);
	    MasterAggregatorHandler handler =
	        new MasterAggregatorHandler(conf, progressable);


	    String tAggName = "T";
	    DoubleWritable tValue = new DoubleWritable(1000);
	    handler.registerPersistentAggregator(tAggName,
	        DoubleSumAggregator.class);
	    handler.setAggregatedValue(tAggName, tValue);

	    
	    String kAggName = "k";
	    DoubleWritable kValue = new DoubleWritable(1000);
	    handler.registerPersistentAggregator(kAggName,
	        DoubleSumAggregator.class);
	    handler.setAggregatedValue(kAggName, kValue);
	    
	    for (AggregatorWrapper<Writable> aggregator :
	        getAggregatorMap(handler).values()) {
	      aggregator.setPreviousAggregatedValue(
	          aggregator.getCurrentAggregatedValue());
	    }

	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    handler.write(new DataOutputStream(out));

	    MasterAggregatorHandler restartedHandler =
	        new MasterAggregatorHandler(conf, progressable);
	    restartedHandler.readFields(
	        new DataInputStream(new ByteArrayInputStream(out.toByteArray())));

	    //two aggregators
	    assertEquals(2, getAggregatorMap(restartedHandler).size());

	    AggregatorWrapper<Writable> tAgg =
	        getAggregatorMap(restartedHandler).get(tAggName);
	    assertTrue(tAgg.getAggregatorClass().equals(DoubleSumAggregator.class));
	    assertEquals(tValue, tAgg.getPreviousAggregatedValue());
	    assertEquals(tValue,restartedHandler.<DoubleWritable>getAggregatedValue(tAggName));
	    assertTrue(tAgg.isPersistent());

	    AggregatorWrapper<Writable> kAgg =
	        getAggregatorMap(restartedHandler).get(kAggName);
	    assertTrue(kAgg.getAggregatorClass().equals
	        (DoubleSumAggregator.class));
	    assertEquals(kValue, kAgg.getPreviousAggregatedValue());
	    assertEquals(kValue,
	        restartedHandler.<DoubleWritable>getAggregatedValue(kAggName));
	    assertTrue(kAgg.isPersistent());
	  }

	

}
