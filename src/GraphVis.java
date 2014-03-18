import java.io.IOException;

import org.apache.giraph.comm.WorkerClientRequestProcessor;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.GraphState;
import org.apache.giraph.graph.GraphTaskManager;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.worker.WorkerAggregatorUsage;
import org.apache.giraph.worker.WorkerContext;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

public class GraphVis
		extends
		BasicComputation<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable, MessageWritable> {

	private static final long W = 1000;
	private static final long L = 1000;
	private static final long AREA = W * L;

	private static final long T = 1000;
	private static final long SPEED = 50;
	
	

	@Override
	public void compute(
			Vertex<IntWritable, CoordinatesPairWritable, EdgeValueTypeWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {

		// Set T in init!

		if (getSuperstep() % 4 == 0) {
			// Generate random position, only at the beginning!
			if (getSuperstep() == 0) {
				CoordinatesWritable pos = new CoordinatesWritable(
						(double) (Math.random() * 1000),
						(double) (Math.random() * 1000));
				CoordinatesWritable disp = new CoordinatesWritable();

				CoordinatesPairWritable coords = new CoordinatesPairWritable(
						pos, disp);

				vertex.setValue(coords);
			}
			
			//set target position to edge values, only in the final iteration
			long t=((LongWritable)getAggregatedValue("temperature")).get();
			if(t==0){
				for(MessageWritable messageWritable : messages){
					for (Edge<IntWritable,EdgeValueTypeWritable> edge : vertex.getEdges()){
						if(edge.getTargetVertexId().compareTo(messageWritable.getSrcId())==0){
							//keep original edge value
							LongWritable edgeValue=edge.getValue().getEdgeValue();
							vertex.setEdgeValue(edge.getTargetVertexId(), 
									new EdgeValueTypeWritable(edgeValue,messageWritable.getPos()));
						}
					}
				}
			}
			
			//send message only when temperature is not 0
			//long t= ((LongWritable) getAggregatedValue("temperature")).get();//don't need to get it again
			if(t != 0){
				//send messages
				int myId = vertex.getId().get();
				// We assume that vertices are numbered 1..n where n is the number
				// of vertices
				for (int i = 1; i <= getTotalNumVertices(); i++) {
					// Send position messages to everyone except self
					if (i != myId) {
						sendMessage(new IntWritable(i),
								new MessageWritable(vertex.getId(), vertex
										.getValue().getPos()));
					}
				}
			}
		} else if (getSuperstep() % 4 == 1) {
			//calculate repulsive forces between everyone except self
			for (MessageWritable messageWritable : messages) {
				
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable disp = new CoordinatesWritable();//v.disp:=0!, will be useful from second iteration
				CoordinatesWritable delta = ownPos.subtract(otherPos);
				double deltaLength = delta.length();
				double frResult=fr(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*frResult/deltaLength,
						delta.getY()*frResult/deltaLength
						);
				disp= disp.add(dispChange);
				//set new disp
				vertex.setValue(new CoordinatesPairWritable(ownPos, disp));
			}
			//send new pos message to neighbors
			//int myId = vertex.getId().get();
			for (Edge<IntWritable,EdgeValueTypeWritable> edge : vertex.getEdges()){
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(vertex.getId(), vertex
								.getValue().getPos()));
			}
			
			
		} else if (getSuperstep() % 4 == 2) {
			//anyone who received a message, calculate displacement, then reply, then move
			//don't need to check anything as the length of empty messages is 0, and not null
			for (MessageWritable messageWritable : messages) {
				//attractive forces
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable disp = vertex.getValue().getDisp();
				CoordinatesWritable delta = ownPos.subtract(otherPos);
				double deltaLength = delta.length();
				double faResult=fa(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*faResult/deltaLength,
						delta.getY()*faResult/deltaLength
						);
				disp= disp.subtract(dispChange);
				//set new disp
				vertex.setValue(new CoordinatesPairWritable(ownPos, disp));
				
				//reply this message
				sendMessage(messageWritable.getSrcId(),
						new MessageWritable(vertex.getId(), vertex
								.getValue().getPos()));
			}
			//move position
			// TODO implement the algorithm for moving
			CoordinatesWritable pos = vertex.getValue().getPos();
			CoordinatesWritable disp = vertex.getValue().getDisp();
			double dispLength = disp.length();
			long t=((LongWritable)getAggregatedValue("temperature")).get();
			//calculate change value, limit it to t
			CoordinatesWritable change= disp.min(t).multiply(new CoordinatesWritable(
					disp.getX()/dispLength,
					disp.getY()/dispLength
					));
			//set new position
			pos=pos.add(change);
			//prevent it from outside frame
			pos.set(Math.min(W/2, Math.max(-W/2, pos.getX())),
					Math.min(L/2, Math.max(-L/2, pos.getY()))
					);
			vertex.setValue(new CoordinatesPairWritable(pos, disp));
			
			
			
			
		} else if (getSuperstep() % 4 == 3) {
			//anyone who received a reply: calculate disp ,then move ,then set edge value to neighbour's pos, then cool
			
			for (MessageWritable messageWritable : messages) {
				//calculate attractive forces
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable disp = vertex.getValue().getDisp();
				CoordinatesWritable delta = ownPos.subtract(otherPos);
				double deltaLength = delta.length();
				double faResult=fa(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*faResult/deltaLength,
						delta.getY()*faResult/deltaLength
						);
				disp= disp.add(dispChange);
				//set new disp
				vertex.setValue(new CoordinatesPairWritable(ownPos, disp));
				
				//set message to respective edge value, only in the final iteration
				long t=((LongWritable)getAggregatedValue("temperature")).get();
				if(t==SPEED){
					for (Edge<IntWritable,EdgeValueTypeWritable> edge : vertex.getEdges()){
						if(edge.getTargetVertexId().compareTo(messageWritable.getSrcId())==0){
							//keep original edge value
							LongWritable edgeValue=edge.getValue().getEdgeValue();
							vertex.setEdgeValue(edge.getTargetVertexId(), 
									new EdgeValueTypeWritable(edgeValue,messageWritable.getPos()));
						}
					}
				}
				
			}
			//move position
			// TODO implement the algorithm for moving
			CoordinatesWritable pos = vertex.getValue().getPos();
			CoordinatesWritable disp = vertex.getValue().getDisp();
			double dispLength = disp.length();
			long t=((LongWritable)getAggregatedValue("temperature")).get();
			//calculate change value, limit it to t
			CoordinatesWritable change= disp.min(t).multiply(new CoordinatesWritable(
					disp.getX()/dispLength,
					disp.getY()/dispLength
					));
			//set new position
			pos=pos.add(change);
			//prevent it from outside frame
			pos.set(Math.min(W/2, Math.max(-W/2, pos.getX())),
					Math.min(L/2, Math.max(-L/2, pos.getY()))
					);
			vertex.setValue(new CoordinatesPairWritable(pos, disp));

			// Cool!
			cool();
			
			//send its position to wake up everyone
			// We assume that vertices are numbered 1..n where n is the number
			// of vertices
			for (int i = 1; i <= getTotalNumVertices(); i++) {
				// Send position messages to everyone including self
					sendMessage(new IntWritable(i),
							new MessageWritable(vertex.getId(), vertex
									.getValue().getPos()));
			}
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
		super.initialize(graphState, workerClientRequestProcessor,
				graphTaskManager, workerAggregatorUsage, workerContext);

		aggregate("temperature", new LongWritable(T));
		aggregate("k", new DoubleWritable(AREA / getTotalNumVertices()));
	}

	private void cool() {
		long currentTemp = ((LongWritable)getAggregatedValue("temperature")).get();
		aggregate("temperature", new LongWritable(currentTemp - SPEED));
	}

	private double fa(double x) {
		double k = ((DoubleWritable)getAggregatedValue("k")).get();
		return x * x / k;
	}

	private double fr(double x) {
		double k = ((DoubleWritable)getAggregatedValue("k")).get();
		return k * k / x;
	}

	@Override
	public void postSuperstep() {
		// TODO Auto-generated method stub
		super.postSuperstep();
	}
}