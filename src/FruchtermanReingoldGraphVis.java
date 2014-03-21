import java.io.IOException;
import java.util.Random;

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







public class FruchtermanReingoldGraphVis
		extends
		BasicComputation<IntWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> {

	private static long W;
	private static long AREA;

	private static double T;
	private static double SPEED;
	private static long LIMIT;//changed
	private static double k;
	private static final double MIN_DIST=0.1;

	@Override
	public void compute(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {

		// Set T in init!

		if (getSuperstep() % 4 == 0) {
			// Generate random position, only at the beginning!
			if (getSuperstep() == 0) {
				Random random = new Random();
				CoordinatesWritable pos = new CoordinatesWritable(
						(double) ((random.nextDouble()-0.5) * 1000),
						(double) ((random.nextDouble()-0.5) * 1000));
				CoordinatesWritable disp = new CoordinatesWritable();

				VertexValueWritable coords = new VertexValueWritable(
						pos, disp);

				vertex.setValue(coords);
			}
			
			//set target position to edge values, only in the final iteration
			//long t=((LongWritable)getAggregatedValue("temperature")).get();
			if(true){//T==0){
				for(MessageWritable messageWritable : messages){
					for (Edge<IntWritable,EdgeValueWritable> edge : vertex.getEdges()){
						if(edge.getTargetVertexId().compareTo(messageWritable.getSrcId())==0){
							//keep original edge value
							LongWritable edgeValue=edge.getValue().getEdgeValue();
							vertex.setEdgeValue(edge.getTargetVertexId(), 
									new EdgeValueWritable(edgeValue,messageWritable.getPos()));
						}
					}
				}
				//messages=null;

			}
			
			//send message only when temperature is > 0
			//long t= ((LongWritable) getAggregatedValue("temperature")).get();//don't need to get it again
			if(T > 0){
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
				
				
				
				//if in the same place, give it a relatively small distance
				if(deltaLength==0){
					deltaLength=MIN_DIST;
				}
				
				
				double frResult=fr(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*frResult/deltaLength,
						delta.getY()*frResult/deltaLength
						);
				disp= disp.add(dispChange);
				//set new disp
				vertex.setValue(new VertexValueWritable(ownPos, disp));
			}
			//messages=null;
			//send new pos message to neighbors
			//int myId = vertex.getId().get();
			for (Edge<IntWritable,EdgeValueWritable> edge : vertex.getEdges()){
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
				
				//if in the same place, give it a relatively small distance
				if(deltaLength==0){
					deltaLength=MIN_DIST;
				}
				
				double faResult=fa(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*faResult/deltaLength,
						delta.getY()*faResult/deltaLength
						);
				disp= disp.subtract(dispChange);
				//set new disp
				vertex.setValue(new VertexValueWritable(ownPos, disp));
				
				//reply this message(can be optimized to send delta)
				sendMessage(messageWritable.getSrcId(),
						new MessageWritable(vertex.getId(), vertex
								.getValue().getPos()));
			}
			//messages=null;
			//move position
			// TODO implement the algorithm for moving
			CoordinatesWritable pos = vertex.getValue().getPos();
			CoordinatesWritable disp = vertex.getValue().getDisp();
			double dispLength = disp.length();
			
			//cannot be 0
			if(dispLength==0){
				dispLength=1;//any number should work, as disp is 0
			}
			
			//long t=((LongWritable)getAggregatedValue("temperature")).get();
			//calculate change value, limit it to t
			CoordinatesWritable change= disp.min(T).multiply(new CoordinatesWritable(
					disp.getX()/dispLength,
					disp.getY()/dispLength
					));
			
			//CoordinatesWritable change =disp;//added
			/*double gravity=10;
			//dispLength: double d = (double) Math.sqrt(pos.getX()*pos.getX() + pos.getY() * pos.getY());
	        double gf = 0.01d * Math.sqrt(1000*1000/128) * (float) gravity * dispLength;
	        change=new CoordinatesWritable( change.getX()-(gf * pos.getX() / dispLength),change.getY()-(gf * pos.getY() / dispLength));
			//to a small number
			change=new CoordinatesWritable(change.getX()/LIMIT,change.getY()/LIMIT);
			//limit*/
			double limitedDist = Math.min(W * ((double) 1 / LIMIT), dispLength);
			change=new CoordinatesWritable(change.getX()/dispLength*limitedDist,change.getY()/dispLength*limitedDist);
			
			//set new position
			pos=pos.add(change);
			
			
			
			
			//prevent it from outside frame
			/*pos.set(Math.min(W/2, Math.max(-W/2, pos.getX())),
					Math.min(L/2, Math.max(-L/2, pos.getY()))
					);*/
			vertex.setValue(new VertexValueWritable(pos, new CoordinatesWritable()));//clear the disp
			
			
			
			
		} else if (getSuperstep() % 4 == 3) {
			//anyone who received a reply: calculate disp ,then move ,then set edge value to neighbour's pos, then cool
			
			for (MessageWritable messageWritable : messages) {
				//calculate attractive forces
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable disp = vertex.getValue().getDisp();
				CoordinatesWritable delta = otherPos.subtract(ownPos);
				double deltaLength = delta.length();
				
				//distance is 0, give it a very small distance
				if(deltaLength==0){
					deltaLength=MIN_DIST;
				}
				
				
				double faResult=fa(deltaLength);
				//calculate the new displacement
				CoordinatesWritable dispChange=new CoordinatesWritable(
						delta.getX()*faResult/deltaLength,
						delta.getY()*faResult/deltaLength
						);
				disp= disp.add(dispChange);
				//set new disp
				vertex.setValue(new VertexValueWritable(ownPos, disp));
				
				//set message to respective edge value, only in the final iteration
				//long t=((LongWritable)getAggregatedValue("temperature")).get();
				if(true){//T<=SPEED){
					for (Edge<IntWritable,EdgeValueWritable> edge : vertex.getEdges()){
						if(edge.getTargetVertexId().compareTo(messageWritable.getSrcId())==0){
							//keep original edge value
							LongWritable edgeValue=edge.getValue().getEdgeValue();
							vertex.setEdgeValue(edge.getTargetVertexId(), 
									new EdgeValueWritable(edgeValue,messageWritable.getPos()));
						}
					}
				}
				
			}
			//messages=null;
			//move position
			// TODO implement the algorithm for moving
			CoordinatesWritable pos = vertex.getValue().getPos();
			CoordinatesWritable disp = vertex.getValue().getDisp();
			double dispLength = disp.length();
			
			//cannot be 0
			if(dispLength==0){
				dispLength=1;
			}
			
			//long t=((LongWritable)getAggregatedValue("temperature")).get();
			//calculate change value, limit it to t
			CoordinatesWritable change= disp.min(T).multiply(new CoordinatesWritable(
					disp.getX()/dispLength,
					disp.getY()/dispLength
					));
			
			/*CoordinatesWritable change =disp;//added
			
			double gravity=10;
			//dispLength: double d = (double) Math.sqrt(pos.getX()*pos.getX() + pos.getY() * pos.getY());
	        double gf = 0.01d * Math.sqrt(1000*1000/128) * (float) gravity * dispLength;
	        change=new CoordinatesWritable( change.getX()-(gf * pos.getX() / dispLength),change.getY()-(gf * pos.getY() / dispLength));
			//to a small number
			change=new CoordinatesWritable(change.getX()/LIMIT,change.getY()/LIMIT);
			//limit*/
			double limitedDist = Math.min(W/2 * ((double) 1 / LIMIT), dispLength);
			change=new CoordinatesWritable(change.getX()/dispLength*limitedDist,change.getY()/dispLength*limitedDist);
			
			//set new position
			pos=pos.add(change);
	
			
			
			
			
			//prevent it from outside frame
			/*pos.set(Math.min(W/2, Math.max(-W/2, pos.getX())),
					Math.min(L/2, Math.max(-L/2, pos.getY()))
					);*/
			vertex.setValue(new VertexValueWritable(pos, disp));

			// Cool!
			if(vertex.getId().get()==1){
			cool();
			}
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
			WorkerClientRequestProcessor<IntWritable, VertexValueWritable, EdgeValueWritable> workerClientRequestProcessor,
			GraphTaskManager<IntWritable, VertexValueWritable, EdgeValueWritable> graphTaskManager,
			WorkerAggregatorUsage workerAggregatorUsage,
			WorkerContext workerContext) {
		super.initialize(graphState, workerClientRequestProcessor,
				graphTaskManager, workerAggregatorUsage, workerContext);
		
		//generate constants by number vertices
		W= getTotalNumVertices() * 10;
		AREA=W*W;
		T=W/10;
		k=Math.sqrt(AREA / getTotalNumVertices());
		SPEED=T/100;
		LIMIT = (long) SPEED*300;
	}

	private void cool() {
		//long currentTemp = ((LongWritable)getAggregatedValue("temperature")).get();
		//aggregate("temperature", new LongWritable(currentTemp - SPEED));
		T=T-SPEED;
	}

	private double fa(double x) {
		//double k = ((DoubleWritable)getAggregatedValue("k")).get();
		return 20000.0d*x * x / k;
	}

	private double fr(double x) {
		//double k = ((DoubleWritable)getAggregatedValue("k")).get();
		return 0.01d*k * k / x;
	}

	
}