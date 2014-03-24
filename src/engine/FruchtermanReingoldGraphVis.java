package engine;
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
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

public class FruchtermanReingoldGraphVis
		extends
		BasicComputation<IntWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> {

	private static double AREA=10000*10000;

	private static double T=1000;
	private static double SPEED=200;
	private static double LIMIT=800;// changed
	private static double k;
	private static final double MIN_DIST = 0.0000001;

	@Override
	public void compute(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {

		// super step
		if (getSuperstep() % 4 == 0) {
			// Generate random position, only at the beginning!
			if (getSuperstep() == 0) {
				generateRandomLayout(vertex);
			}
			
			// set target position to edge values, only in the final iteration
			if (T <= 0) {
				setEdgeValuesForOutPut(vertex, messages);

			}

			// send messages
			sendOwnPositionToEveryVertex(vertex);
			

		} else if (getSuperstep() % 4 == 1) {
			
			
			//move position
			moveVertex(vertex);
			if(T > 0){
			// calculate repulsive forces between everyone except self
			calculateRepulsiveForces(vertex, messages);

			// send new pos message to neighbors
			sendOwnPositionToNeighbors(vertex);
			}

		} else if (getSuperstep() % 4 == 2) {
			// anyone who received a message, calculate displacement, then
			// reply, then move
			// don't need to check anything as the length of empty messages is
			// 0, and not null
			for (MessageWritable messageWritable : messages) {
				// attractive forces
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable disp = vertex.getValue().getDisp();
				CoordinatesWritable delta = ownPos.subtract(otherPos);
				
				CoordinatesWritable dispChange = calculateAttractiveForce(delta);
				disp = disp.subtract(dispChange);
				// set new disp
				vertex.setValue(new VertexValueWritable(ownPos, disp));
				
				
				// only send delta to reply
				sendMessage(messageWritable.getSrcId(), new MessageWritable(
						vertex.getId(), delta));
			}



		} else if (getSuperstep() % 4 == 3) {
			// anyone who received a reply: calculate disp ,then move ,then set
			// edge value to neighbour's pos, then cool

			for (MessageWritable messageWritable : messages) {
				// calculate attractive forces
				CoordinatesWritable ownPos = vertex.getValue().getPos();
				CoordinatesWritable disp = vertex.getValue().getDisp();
				CoordinatesWritable delta = messageWritable.getPos();
				
				CoordinatesWritable dispChange = calculateAttractiveForce(delta);
				disp = disp.add(dispChange);
				// set new disp
				vertex.setValue(new VertexValueWritable(ownPos, disp));


			}



			// Cool!
			if (vertex.getId().get() == 1) {
				cool();
			}

			// send its position to wake up everyone
			sendOwnPositionToEveryVertex(vertex);
		}

		vertex.voteToHalt();
	}

	private CoordinatesWritable calculateAttractiveForce(
			CoordinatesWritable delta) {
		double deltaLength = delta.length();

		// if in the same place, give it a relatively small distance
		if (deltaLength == 0) {
			deltaLength = MIN_DIST;
		}

		double faResult = fa(deltaLength);
		// calculate the new displacement
		CoordinatesWritable dispChange = new CoordinatesWritable(delta.getX()
				* faResult / deltaLength, delta.getY() * faResult / deltaLength);
		return dispChange;
	}

	/**
	 * @param vertex
	 */
	private void moveVertex(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		CoordinatesWritable pos = vertex.getValue().getPos();
		CoordinatesWritable disp = vertex.getValue().getDisp();
		double dispLength = disp.length();

		// cannot be 0
		if (dispLength == 0) {
			dispLength = 1;// any number should work, as disp is 0
		}

		// calculate change value, limit it to t
		CoordinatesWritable change = disp.min(T).multiply(
				new CoordinatesWritable(disp.getX() / dispLength, disp.getY()
						/ dispLength));
		
		
		//gravity
		//CoordinatesWritable change =disp;//added
		double gravity=10;
		double d = (double) Math.sqrt(pos.getX()*pos.getX() + pos.getY() * pos.getY());
		double gf = 0.01d * k * (double) gravity * d;
		change=new CoordinatesWritable( change.getX()-(gf * pos.getX() / d),change.getY()-(gf * pos.getY() / d));
		
		
		//to a small number
		change=new CoordinatesWritable(change.getX()/LIMIT,change.getY()/LIMIT);
		
		
		
		// limit
		double limitedDist = Math.min((Math.sqrt(AREA)/10) * ((double) 1 / LIMIT), dispLength);
		change = new CoordinatesWritable(change.getX() / dispLength
				* limitedDist, change.getY() / dispLength * limitedDist);

		// set new position
		pos = pos.add(change);
		vertex.setValue(new VertexValueWritable(pos, new CoordinatesWritable()));// clear
																					// the
																					// disp
	}

	/**
	 * @param vertex
	 */
	private void sendOwnPositionToNeighbors(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
			sendMessage(edge.getTargetVertexId(),
					new MessageWritable(vertex.getId(), vertex.getValue()
							.getPos()));
		}
	}

	/**
	 * @param vertex
	 * @param messages
	 */
	private void calculateRepulsiveForces(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) {
		
		for (MessageWritable messageWritable : messages) {

			CoordinatesWritable ownPos = vertex.getValue().getPos();
			CoordinatesWritable otherPos = messageWritable.getPos();
			CoordinatesWritable disp = vertex.getValue().getDisp();
			CoordinatesWritable delta = ownPos.subtract(otherPos);
			double deltaLength = delta.length();

			// if in the same place, give it a relatively small distance
			if (deltaLength == 0) {
				deltaLength = MIN_DIST;
			}

			double frResult = fr(deltaLength);
			// calculate the new displacement
			CoordinatesWritable dispChange = new CoordinatesWritable(
					delta.getX() * frResult / deltaLength, delta.getY()
							* frResult / deltaLength);
			disp = disp.add(dispChange);
			// set new disp
			vertex.setValue(new VertexValueWritable(ownPos, disp));
		}
	}

	/**
	 * @param vertex
	 */
	private void sendOwnPositionToEveryVertex(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		int myId = vertex.getId().get();
		// We assume that vertices are numbered 1..n where n is the number
		// of vertices
		for (int i = 1; i <= getTotalNumVertices(); i++) {
			// Send position messages to everyone except self
			if (i != myId) {
				sendMessage(new IntWritable(i),
						new MessageWritable(vertex.getId(), vertex.getValue()
								.getPos()));
			}
		}
	}

	/**
	 * @param vertex
	 * @param messages
	 */
	private void setEdgeValuesForOutPut(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) {
		for (MessageWritable messageWritable : messages) {
			for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				if (edge.getTargetVertexId().compareTo(
						messageWritable.getSrcId()) == 0) {
					// keep original edge value
					LongWritable edgeValue = edge.getValue().getEdgeValue();
					vertex.setEdgeValue(
							edge.getTargetVertexId(),
							new EdgeValueWritable(edgeValue, messageWritable
									.getPos()));
				}
			}
		}
	}

	/**
	 * @param vertex
	 */
	private void generateRandomLayout(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		Random random = new Random();
		CoordinatesWritable pos = new CoordinatesWritable(
				(double) ((random.nextDouble() - 0.5) * 1000),
				(double) ((random.nextDouble() - 0.5) * 1000)
				);
		CoordinatesWritable disp = new CoordinatesWritable();

		VertexValueWritable coords = new VertexValueWritable(pos, disp);

		vertex.setValue(coords);
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
		k = Math.sqrt(AREA / (double)getTotalNumVertices());
		
	}

	private void cool() {

		T = T - SPEED;
	}

	private double fa(double x) {
		//2000000000.0d *
		return x * x / k;
	}

	private double fr(double x) {

		return  k * k / x;
	}

}