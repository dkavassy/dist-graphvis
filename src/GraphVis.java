import java.io.IOException;

import org.apache.giraph.comm.WorkerClientRequestProcessor;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.GraphState;
import org.apache.giraph.graph.GraphTaskManager;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.worker.WorkerAggregatorUsage;
import org.apache.giraph.worker.WorkerContext;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

public class GraphVis extends BasicComputation<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable, MessageWritable> {

	private static final long W = 1000;
	private static final long L = 1000;
	private static final long AREA = W*L;
	
	private static final long T = 1000;
	
	@Override
	public void compute(
			Vertex<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {
		 
		// Set T in init!
		
		 if (getSuperstep() % 4 == 0) {
			 
			 // Generate random position, only at the beginning!
			 if (getSuperstep() == 0)
			 {
				 CoordinatesWritable pos  = new CoordinatesWritable( (long) (Math.random()*1000), (long) (Math.random()*1000) );
				 CoordinatesWritable disp = new CoordinatesWritable();
				 
				 CoordinatesPairWritable coords = new CoordinatesPairWritable(pos, disp);
				 
			     vertex.setValue(coords);
			 }
			 
			 int myId = vertex.getId().get();
			 
			 // We assume that vertices are numbered 1..n where n is the number of vertices
			 for (int i=1;i <= getTotalNumVertices(); i++)
			 {
				 // Send messages to everyone except self
				 if (i != myId) {
					 sendMessage(new IntWritable(i),
						 new MessageWritable(vertex.getId(),
								 vertex.getValue().getPos()));
				 }
			 }	 
		 }
		 else if (getSuperstep() % 4 == 1)
		 {
			 for (MessageWritable messageWritable : messages) {
				 CoordinatesWritable ownPos =   vertex.getValue().getPos();
				 CoordinatesWritable otherPos = messageWritable.getPos();
				 
				 CoordinatesWritable disp = vertex.getValue().getDisp();
				 
				 CoordinatesWritable delta = ownPos.subtract(otherPos);
				 
				 double deltaLength = delta.length();
				 
				 disp.set(disp.getX(), disp.getY());
				 
				 vertex.setValue(new CoordinatesPairWritable(ownPos, disp));
			 }
		 }
		 else if (getSuperstep() % 4 == 2)
		 {

		 }
		 else if (getSuperstep() % 4 == 3)
		 {
			 // Cool!
			 cool();
		 }
		 
		 vertex.voteToHalt();
	}
	
	@Override
	public void initialize(
			GraphState graphState,
			WorkerClientRequestProcessor<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable> workerClientRequestProcessor,
			GraphTaskManager<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable> graphTaskManager,
			WorkerAggregatorUsage workerAggregatorUsage,
			WorkerContext workerContext) {
		super.initialize(graphState, workerClientRequestProcessor, graphTaskManager,
				workerAggregatorUsage, workerContext);
		
		aggregate("temperature", new LongWritable(T));
		aggregate( "k", new DoubleWritable( AREA/getTotalNumVertices() ) );
	}
	
	private void cool() {
		long currentTemp = getAggregatedValue("temperature");
		aggregate("temperature",
				new LongWritable(currentTemp - 5L));
	}
	
	private double fa(double x) {
		double k = getAggregatedValue("k");
		return x*x/k;
	}

	private double fr(double x) {
		double k = getAggregatedValue("k");
		return k*k/x;
	}
	
	@Override
	public void postSuperstep() {
		// TODO Auto-generated method stub
		super.postSuperstep();
	}
}