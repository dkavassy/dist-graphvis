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

	private static final int SUPERSTEPS = 6;
	
	private static final double W = 100.0, L = 100.0;
	private static final double AREA = W*L;
	
	private static final double MIN_DIST = 0.1;
	
	private static final double SPEED = 1.0;
	
	private static final double LIMIT = W/10.0;
	
	private static final double k = Math.sqrt(AREA / 4); // Assuming numVertices == 4
	
	private static double t = W/10.0;

	@Override
	public void compute(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) throws IOException {

		// Super Step 1
		if (getSuperstep() % SUPERSTEPS == 0) {
			// Everybody is awake
			
			// Very first superstep: init in random position
			if (getSuperstep() == 0) {
				
				Random random = new Random();
				CoordinatesWritable pos = new CoordinatesWritable(
						vertex.getId().get() * 10, //(random.nextDouble() - 0.5) * 100.0,
						vertex.getId().get() * 5   //(random.nextDouble() - 0.5) * 100.0
						);
				
				CoordinatesWritable disp        = new CoordinatesWritable(0.0,0.0);
				VertexValueWritable vertexValue = new VertexValueWritable(pos, disp);

				vertex.setValue(vertexValue);
			}
			else {
				// If it's not the very first superstep, let's chill!
				if (vertex.getId().get() == 1) {
					cool();
				}
			}
			
			// If we're not frozen yet, wake everyone up and send own position to everyone for next SS
			if (t > 0) {
				sendOwnPositionToEveryVertex(vertex);
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 1) {
			// Everybody is awake
			
			// calculate repulsive forces between everyone except self
			applyRepulsiveForces(vertex, messages);

			// Send position to neighbors
			for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(vertex.getId(), vertex.getValue().getPos()));
			}

		}
		else if (getSuperstep() % SUPERSTEPS == 2) {
			// Vertexes with in-edges are awake
			
			// anyone who received a message, calculate displacement, then
			// reply and wait, because in the next step they might get more messages
			// from vertices which are connected by out-edges
			
			// param3 true: send a message back with position
			applyAttractiveForces(vertex, messages, true);
			
			// if there are no out-edges, move, otherwise wait until next step and move then
			if (vertex.getNumEdges() == 0) {
				moveVertex(vertex);
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 3) {
			// Vertices with out-edges are awake

			// param3 true: no need to send another message back
			applyAttractiveForces(vertex, messages, false);
			
			// move, those who don't have out-edges have already moved
			moveVertex(vertex);

			// Wake up vertices with in-edges
			for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(vertex.getId(), new CoordinatesWritable()));
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 4) {
			// Vertices with in-edges are awake
			
			CoordinatesWritable ownPos = vertex.getValue().getPos();
			IntWritable         ownId  = vertex.getId();
			
			// Send updated position back to everyone from whom a msg was rcvd
			for (MessageWritable messageWritable : messages) {
				sendMessage(messageWritable.getSrcId(),
						new MessageWritable(ownId, ownPos));
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 5) {
			// Vertices with out-edges are awake
			
			// Set neighbor's position in edge value
			for (MessageWritable messageWritable : messages) {
				
				IntWritable srcId = messageWritable.getSrcId();
				
				for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
					
					if (edge.getTargetVertexId().equals(srcId)) {
						
						// Keep weight
						LongWritable weight = edge.getValue().getWeight();
						
						vertex.setEdgeValue(
								edge.getTargetVertexId(),
								new EdgeValueWritable(weight, messageWritable.getPos()));
					}
				}
			}
			
			// Wake everyone up for the next superstep
			sendOwnPositionToEveryVertex(vertex);
		}

		vertex.voteToHalt();
	}
	
	/**
	 * @param vertex
	 * @param messages
	 */
	private void applyRepulsiveForces(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) {
		
		VertexValueWritable vertexValue = vertex.getValue();
		CoordinatesWritable ownPos      = vertexValue.getPos();
		// We start with zero displacement
		CoordinatesWritable disp        = new CoordinatesWritable(0.0,0.0);
		
		for (MessageWritable messageWritable : messages) {
			
			CoordinatesWritable otherPos = messageWritable.getPos();
			
			CoordinatesWritable delta    = ownPos.subtract(otherPos);
			double deltaLength           = delta.length();

			// if in the same place, give it a relatively small distance
			if (deltaLength < MIN_DIST) { // avoid comparing doubles for equality!
				deltaLength = MIN_DIST;
			}

			// calculate the new displacement
			CoordinatesWritable dispChange = delta.divide(deltaLength).multiply(fr(deltaLength));
			
			// Update displacement
			disp = disp.add(dispChange);
		}
		
		// set new disp
		vertex.setValue(new VertexValueWritable(ownPos, disp));
	}

	private void applyAttractiveForces(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages,
			boolean sendMessageBack) {
		
		VertexValueWritable vertexValue = vertex.getValue();
		CoordinatesWritable ownPos      = vertexValue.getPos();
		CoordinatesWritable disp        = vertexValue.getDisp();
		IntWritable         ownId       = vertex.getId();
		
		// don't need to check anything as the length of empty messages is
		// 0, and not null
		for (MessageWritable messageWritable : messages) {
			
			// attractive forces
			CoordinatesWritable otherPos = messageWritable.getPos();
			
			CoordinatesWritable delta = ownPos.subtract(otherPos);
			
			double deltaLength = delta.length();

			// if in the same place, give it a relatively small distance
			if (deltaLength < MIN_DIST) {
				deltaLength = MIN_DIST;
			}
			
			// calculate the new displacement
			CoordinatesWritable dispChange = delta.divide(deltaLength).multiply(fa(deltaLength));
			
			// Equivalent to subtraction for in-edge, addition for out-edge as per original paper
			disp = disp.subtract(dispChange);
			
			// set new disp
			vertex.setValue(new VertexValueWritable(ownPos, disp));
			
			// // Send a message back on in-edge (only in first iteration)
			if (sendMessageBack) {
				sendMessage(messageWritable.getSrcId(),
						new MessageWritable(ownId, ownPos));
			}
		}
		
		vertex.setValue(new VertexValueWritable(ownPos, disp));
	}
	
	private void moveVertex(Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		
		VertexValueWritable vertexValue = vertex.getValue();
		CoordinatesWritable pos         = vertexValue.getPos();
		CoordinatesWritable disp        = vertexValue.getDisp();
		
		double dispLength = disp.length();
		
		if (dispLength < MIN_DIST) {
			dispLength = 1.0; // Anything is fine here as long as it's not 0
		}
		
		// Limit displacement with temperature
		CoordinatesWritable actualDisplacement = new CoordinatesWritable(1, 5);
			//	.divide(dispLength) // normalized vector
			//	.multiply( disp.min(t) );
		
		// Add displacement
		CoordinatesWritable newPos = pos.add(actualDisplacement);
		
		//double x = newPos.getX(), y = newPos.getY();
		/*// Limit position with frame
		if (x > W/2) {
			x = W/2;
		}
		else if (x < -W/2) {
			x = -W/2;
		}
		
		if (y > L/2) {
			y = L/2;
		}
		else if (y < -L/2) {
			y = -L/2;
		}*/
		
		// Set new position and reset disp
		//newPos = new CoordinatesWritable(x, y);
		vertex.setValue(new VertexValueWritable(newPos, new CoordinatesWritable(0.0,0.0)));
	}

	/**
	 * @param vertex
	 */
	private void move(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		
		CoordinatesWritable pos = vertex.getValue().getPos();
		CoordinatesWritable disp = vertex.getValue().getDisp();
		double dispLength = disp.length();

		// cannot be 0
		if (dispLength == 0) {
			dispLength = 1;// any number should work, as disp is 0
		}

		// calculate change value, limit it to t
		CoordinatesWritable change = disp.min(t).multiply(
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
		double limitedDist = Math.min((Math.sqrt(AREA)/10) * (1.0 / LIMIT), dispLength);
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
	private void sendOwnPositionToEveryVertex(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		
		int myId = vertex.getId().get();
		// We assume that vertices are numbered 1..n where n is the number
		// of vertices
		for (int i = 1; i <= getTotalNumVertices(); i++) {
			// Send position messages to everyone except self
			if (i != myId) {
				sendMessage(new IntWritable(i),
						new MessageWritable(vertex.getId(),
								vertex.getValue().getPos()));
			}
		}
	}

	// Linear cooling function
	private void cool() {
		t = t - SPEED;
	}

	// Attractive force
	private double fa(double x) {
		
		return x*x / k;
	}

	// Repulsive force
	private double fr(double z) {

		return  k*k / z;
	}

}