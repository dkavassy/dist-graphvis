/*
 * @(\#) FruchtermanReingoldGraphVis.java 1.1 10 March 20
 *
 * Copyright (\copyright) 2014 University of York & British Telecommunications plc
 * This Software is granted under the MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package engine;

import java.io.IOException;
import java.util.Random;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.EdgeReader;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/** 
 * A class that represents coordinates of a vector. 
 * This class contain x and y coordinates. 
 * <p> 
 * @author Shan Huang 
 * @author Daniel Kavassy 
 * @version 1.1 Initial development. 
 */ 
public class FruchtermanReingoldGraphVis extends BasicComputation<IntWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> {
	
	//number of supersteps per iteration
	private static final int SUPERSTEPS = 6;
	//canvas height and width
	private static final double W = 10000.0, L = 10000.0;
	//the area of graph visualization
	private static final double AREA = W*L;
	//minimum distance between two vertices
	private static final double MIN_DIST = 0.000001;
	//amount of temperature goes down after each iteration
	private static final double SPEED = W/1000.0;
	//limit the distance in which a vertex can move from the center
	private static final double LIMIT = 10.0;
	
	private static double k;
	private static double T = W/10.0;

	
	/**
	* Apply the FruchtermanReingold algorithm on every vertex. Divided in to 6 supersteps.
	* @param vertex the vertex to calculate on
	* @param messages messages received from previous superstep
	* @throws IOException
	*/
	@Override
	public void compute(
			Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) 
					throws IOException {

		// Super Step 0
		if (getSuperstep() % SUPERSTEPS == 0) {
			// Everybody is awake
			
			// Very first superstep: init in random position
			if (getSuperstep() == 0) {
				
				Random random = new Random();
				CoordinatesWritable pos = new CoordinatesWritable(
						(random.nextDouble() - 0.5) * 100.0,
						(random.nextDouble() - 0.5) * 100.0
						);
				
				CoordinatesWritable disp        = new CoordinatesWritable(0.0,0.0);
				VertexValueWritable vertexValue = new VertexValueWritable(pos, disp);
				vertex.setValue(vertexValue);
				k = Math.sqrt(AREA / getTotalNumVertices());
			}else {
				// If it's not the very first superstep, let's chill!
				if (vertex.getId().get() == 1) {
					cool();
				}
			}
			
			// If we're not frozen yet, wake everyone up and send own position to everyone for next superstep
			if (T > 0 || (T < MIN_DIST && T > -MIN_DIST && vertex.getId().get() == 1)) {
				
				IntWritable ownId = vertex.getId();
				int intOwnId      = ownId.get();
				
				CoordinatesWritable ownPos = new CoordinatesWritable(vertex.getValue().getPos().getX(), vertex.getValue().getPos().getY());
				
				long totalVertices = getTotalNumVertices();
				
				// We assume that vertices are numbered 1..n where n is the number
				// of vertices
				for (int i = 1; i <= totalVertices; i++) {
					// Send position messages to everyone except self
					if (i != intOwnId) {
						sendMessage(
								new IntWritable(i),
								new MessageWritable(ownId, ownPos)
								);
					}
				}
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 1) {
			// Everybody is awake
			
			// calculate repulsive forces between everyone
			VertexValueWritable vertexValue  = vertex.getValue();
			CoordinatesWritable pos          = vertexValue.getPos();
			// We start with zero displacement
			CoordinatesWritable disp        = new CoordinatesWritable(0.0,0.0);
			
			for (MessageWritable messageWritable : messages) {
				
				CoordinatesWritable otherPos = messageWritable.getPos();
				CoordinatesWritable delta    = pos.subtract(otherPos);
				double deltaLength           = delta.length();
			
				// if dots are in the same place, let's try to separate them
				if (deltaLength < MIN_DIST) {
					delta = makeUpDelta();
					deltaLength = delta.length();
				}
				
				// Update displacement
				disp = disp.add( delta.multiply(fr(deltaLength)/deltaLength) );
			}
			
			// set new disp
			vertex.setValue(new VertexValueWritable(pos, disp));
			
			// Send position to neighbors
			CoordinatesWritable ownPos = new CoordinatesWritable(pos.getX(), pos.getY());
			IntWritable         ownId  = vertex.getId();
			
			for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(ownId, ownPos));
			}

		}
		else if (getSuperstep() % SUPERSTEPS == 2) {
			// Vertices with in-edges are awake
			
			// anyone who received a message, calculate displacement, then
			// reply and wait, because in the next step they might get more messages
			// from vertices which are connected by out-edges
			
			// param3 true: send a message back with position
			applyAttractiveForces(vertex, messages, true);
			
			// if there are no out-edges, move, otherwise wait until next step and move then
			if (vertex.getNumEdges() == 0) {
				move(vertex);
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 3) {
			// Vertices with out-edges are awake

			// param3 true: no need to send another message back
			applyAttractiveForces(vertex, messages, false);
			
			// move, those who don't have out-edges have already moved
			move(vertex);

			// Wake up vertices with in-edges
			for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(vertex.getId(), new CoordinatesWritable()));
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 4) {
			// Vertices with in-edges are awake

			IntWritable         ownId  = new IntWritable(vertex.getId().get());
			
			CoordinatesWritable ownPos = new CoordinatesWritable(vertex.getValue().getPos().getX(),
					vertex.getValue().getPos().getY());
			
			// Send new position back to everyone from whom a msg was rcvd
			for (MessageWritable messageWritable : messages) {
				
				sendMessage(messageWritable.getSrcId(),
						new MessageWritable(ownId, ownPos));
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 5) {
			// Vertices with out-edges are awake
			
			// Set neighbor's position in edge value
			for (MessageWritable messageWritable : messages) {
				
				int               srcId = messageWritable.getSrcId().get();
				CoordinatesWritable pos = new CoordinatesWritable(messageWritable.getPos().getX(),
						messageWritable.getPos().getY());
				
				for (Edge<IntWritable, EdgeValueWritable> edge : vertex.getEdges()) {
					
					int targetId = edge.getTargetVertexId().get();
					
					if (targetId == srcId) {
						
						// Keep weight
						LongWritable weight = new LongWritable(edge.getValue().getWeight().get());
						
						vertex.setEdgeValue(
								new IntWritable(targetId),
								new EdgeValueWritable(weight, pos));
					}
				}
			}
			
			// Wake everyone up for the next superstep, including self
			long totalVertices = getTotalNumVertices();
			for (int i = 1; i <= totalVertices; i++) {
					sendMessage(new IntWritable(i), new MessageWritable());
			}
		}

		vertex.voteToHalt();
	}
	
	
	/**
	* generate random distance coordinates if two vertices are in the same position
	* coordinates should be between (-0.5,-0.5) to (0.5,0.5)
	* @return new CoordinatesWritable with random x and y
	*/
	private CoordinatesWritable makeUpDelta() {
		return new CoordinatesWritable((new Random()).nextDouble()-0.5, (new Random()).nextDouble()-0.5);
	}

	
	/**
	* Calculate the attractive forces between a vertex and its neighbors. Reply its own position if it has in-edges.
	* @param vertex the vertex to calculate on
	* @param messages the messages with neighbors' positions
	* @param sendMessageBack a boolean to control whether to reply messages
	*/
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
				delta = makeUpDelta();
				deltaLength = delta.length();
			}
			
			// Equivalent to subtraction for in-edge, addition for out-edge as per original paper
			disp = disp.subtract( delta.multiply(fa(deltaLength)/deltaLength) );
			
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
	

	/**
	* Move a vertex according to its displacement. Apply gravity to avoid vertices moving too far away from the center of graph.
	* Limit the speed of movement of vertices so as to prevent vertices from moving outside the canvas.
	* @param vertex the vertex to move
	*/
	private void move(Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		
		CoordinatesWritable pos = vertex.getValue().getPos();
		CoordinatesWritable disp = vertex.getValue().getDisp();
		double dispLength = disp.length();

		// cannot be 0
		if (dispLength == 0) {
			dispLength = 1;// any number should work, as disp is 0
		}
		
		//gravity
		CoordinatesWritable change =disp;//added
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
		vertex.setValue(new VertexValueWritable(pos, new CoordinatesWritable()));
	}

	/**
	* Subtract the temperature by SPEED to cool down the whole environment to 
	* prevent vertices from moving permanently
	*/
	// Linear cooling function
	private void cool() {
		T = T - SPEED;
	}

	
	/**
	* The function for calculating attractive force.
	* @param x the distance of two vertices
	* @return a double which is the attractive force
	*/
	private double fa(double x) {
		
		return x*x / k;
	}

	/**
	* The function for calculating repulsive force.
	* @param z the distance of two vertices
	* @return a double which is the repulsive force
	*/
	private double fr(double z) {

		return  k*k / z;
	}
	
	public static double getT() {
		return T;
	}
	
	public static double getK() {
		return k;
	}

	
}