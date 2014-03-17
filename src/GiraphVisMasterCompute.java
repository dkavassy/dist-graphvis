import org.apache.giraph.aggregators.DoubleOverwriteAggregator;
import org.apache.giraph.aggregators.LongOverwriteAggregator;
import org.apache.giraph.master.DefaultMasterCompute;


public class GiraphVisMasterCompute extends DefaultMasterCompute {

	@Override
	public void initialize() throws InstantiationException,
			IllegalAccessException {
		super.initialize();
		
		registerPersistentAggregator("temperature", LongOverwriteAggregator.class);
		registerPersistentAggregator("k", DoubleOverwriteAggregator.class);
	}

}
