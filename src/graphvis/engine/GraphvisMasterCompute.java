package graphvis.engine;


import org.apache.giraph.aggregators.*;
import org.apache.giraph.master.DefaultMasterCompute;

public class GraphvisMasterCompute extends DefaultMasterCompute {

	@Override
	public void initialize() throws InstantiationException,
			IllegalAccessException {
		
		//The force factor
		registerPersistentAggregator("k", DoubleSumAggregator.class);
		//The temperature
		registerPersistentAggregator("T", DoubleSumAggregator.class);
		
	}

}
