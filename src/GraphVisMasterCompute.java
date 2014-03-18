import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.giraph.aggregators.DoubleOverwriteAggregator;
import org.apache.giraph.aggregators.LongOverwriteAggregator;
import org.apache.giraph.master.MasterCompute;


public class GraphVisMasterCompute extends MasterCompute {



	@Override
	public void initialize() throws InstantiationException,
			IllegalAccessException {
		registerPersistentAggregator("k", DoubleOverwriteAggregator.class);
		registerPersistentAggregator("temperature", LongOverwriteAggregator.class);
	}

	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void compute() {
		// TODO Auto-generated method stub
		
	}

}
