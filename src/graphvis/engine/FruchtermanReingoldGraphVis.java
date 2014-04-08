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

package graphvis.engine;

import graphvis.type.VectorWritable;
import graphvis.type.EdgeValueWritable;
import graphvis.type.MessageWritable;
import graphvis.type.VertexValueWritable;

import java.io.IOException;
import java.util.Random;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.worker.WorkerAggregatorUsage;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;

/** 
 * A class that represents coordinates of a vector. 
 * This class contain x and y coordinates. 
 * <p> 
 * @author Shan Huang 
 * @author Daniel Kavassy 
 * @version 1.1 Initial development. 
 */ 
public class FruchtermanReingoldGraphVis extends BasicComputation<LongWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> {
	
	//number of supersteps per iteration
	private static final int SUPERSTEPS = 6;
	//canvas height and width
	private static final double W = 10000.0, L = 10000.0;
	//the area of graph visualization
	private static final double AREA = W*L;
	//minimum distance between two vertices
	private static final double MIN_DIST = 0.000001;
	//amount of temperature goes down after each iteration
	private static final double SPEED = W/100.0;
	//limit the velocity with which a vertex can move away from the center
	private static final double LIMIT = 3.0;
	//Temperature and K are in aggregators. See GraphvisMasterCompute.java.

	
	private WorkerAggregatorUsage aggregator = this;
	



	/**
	* Apply the FruchtermanReingold algorithm on every vertex. Divided in to 6 supersteps.
	* @param vertex the vertex to calculate on
	* @param messages messages received from previous superstep
	* @throws IOException
	*/
	@Override
	public void compute(
			Vertex<LongWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages) 
					throws IOException {

		// Super Step 0
		if (getSuperstep() % SUPERSTEPS == 0) {
			// Everybody is awake
			//Get default aggregator values.
			double T = ((DoubleWritable) aggregator.getAggregatedValue("T")).get();
			double k = ((DoubleWritable) aggregator.getAggregatedValue("k")).get();
			// Very first superstep: init in random position
			if (getSuperstep() == 0) {
				Random random = new Random();
				VectorWritable pos = new VectorWritable(
						(random.nextDouble() - 0.5) * 100.0,
						(random.nextDouble() - 0.5) * 100.0
						);
				
				VectorWritable disp             = new VectorWritable(0.0,0.0);
				VertexValueWritable vertexValue = new VertexValueWritable(pos, disp);
				vertex.setValue(vertexValue);
				if (vertex.getId().get() == 1) {
					
				//Initialize aggregator values.
					aggregator.aggregate("k", new DoubleWritable(-k+Math.sqrt(AREA / getTotalNumVertices())));
					aggregator.aggregate("T", new DoubleWritable(-T+W/10));
				T = W/10;
				}
			}else {
				// If it's not the very first superstep, let's chill!
				if (vertex.getId().get() == 1) {
					//cool
					aggregator.aggregate("T", new DoubleWritable(-SPEED));
					T = T - SPEED;
				}
			}
			
			
			// If we're not frozen yet, wake everyone up and send own position to everyone for next superstep
			if (T > 0 || (T < MIN_DIST && T > -MIN_DIST && vertex.getId().get() == 1)) {
				
				LongWritable ownId = vertex.getId();
				long intOwnId      = ownId.get();
				
				VectorWritable ownPos = new VectorWritable(vertex.getValue().getPos().getX(), vertex.getValue().getPos().getY());
				
				long totalVertices = getTotalNumVertices();
				
				// We assume that vertices are numbered 1..n where n is the number
				// of vertices
				for (long i = 1; i <= totalVertices; i++) {
					// Send position messages to everyone except self
					if (i != intOwnId) {
						sendMessage(
								new LongWritable(i),
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
			VectorWritable pos               = vertexValue.getPos();
			// We start with zero displacement
			VectorWritable disp        = new VectorWritable(0.0,0.0);
			
			for (MessageWritable messageWritable : messages) {
				
				VectorWritable otherPos = messageWritable.getPos();
				VectorWritable delta    = pos.subtract(otherPos);
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
			VectorWritable ownPos = new VectorWritable(pos.getX(), pos.getY());
			LongWritable   ownId  = vertex.getId();
			
			for (Edge<LongWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
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
			for (Edge<LongWritable, EdgeValueWritable> edge : vertex.getEdges()) {
				
				sendMessage(edge.getTargetVertexId(),
						new MessageWritable(vertex.getId(), new VectorWritable()));
			}
		}
		else if (getSuperstep() % SUPERSTEPS == 4) {
			// Vertices with in-edges are awake

			LongWritable ownId  = new LongWritable(vertex.getId().get());
			
			VectorWritable ownPos = new VectorWritable(vertex.getValue().getPos().getX(),
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
				
				long         srcId = messageWritable.getSrcId().get();
				
				VectorWritable pos = new VectorWritable(messageWritable.getPos().getX(),
						messageWritable.getPos().getY());
				
				for (Edge<LongWritable, EdgeValueWritable> edge : vertex.getEdges()) {
					
					long targetId = edge.getTargetVertexId().get();
					
					if (targetId == srcId) {
						
						// Keep weight
						LongWritable weight = new LongWritable(edge.getValue().getWeight().get());
						
						vertex.setEdgeValue(
								new LongWritable(targetId),
								new EdgeValueWritable(weight, pos));
					}
				}
			}
			
			// Wake everyone up for the next superstep, including self
			long totalVertices = getTotalNumVertices();
			for (int i = 1; i <= totalVertices; i++) {
					sendMessage(new LongWritable(i), new MessageWritable());
			}
		}

		vertex.voteToHalt();
	}
	
	
	/**
	* generate random distance coordinates if two vertices are in the same position
	* coordinates should be between (-0.5,-0.5) to (0.5,0.5)
	* @return new CoordinatesWritable with random x and y
	*/
	private VectorWritable makeUpDelta() {
		return new VectorWritable((new Random()).nextDouble()-0.5, (new Random()).nextDouble()-0.5);
	}

	
	/**
	* Calculate the attractive forces between a vertex and its neighbors. Reply its own position if it has in-edges.
	* @param vertex the vertex to calculate on
	* @param messages the messages with neighbors' positions
	* @param sendMessageBack a boolean to control whether to reply messages
	*/
	private void applyAttractiveForces(
			Vertex<LongWritable, VertexValueWritable, EdgeValueWritable> vertex,
			Iterable<MessageWritable> messages,
			boolean sendMessageBack) {
		
		VertexValueWritable vertexValue = vertex.getValue();
		VectorWritable           ownPos = vertexValue.getPos();
		VectorWritable             disp = vertexValue.getDisp();
		LongWritable              ownId = vertex.getId();
		
		// don't need to check anything as the length of empty messages is
		// 0, and not null
		for (MessageWritable messageWritable : messages) {
			
			// attractive forces
			VectorWritable otherPos = messageWritable.getPos();
			
			VectorWritable delta = ownPos.subtract(otherPos);
			
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
	private void move(Vertex<LongWritable, VertexValueWritable, EdgeValueWritable> vertex) {
		
		VectorWritable pos  = vertex.getValue().getPos();
		VectorWritable disp = vertex.getValue().getDisp();
		double dispLength = disp.length();

		// cannot be 0
		if (dispLength == 0) {
			dispLength = 1;// any number should work, as disp is 0
		}
		
		double k = ((DoubleWritable) aggregator.getAggregatedValue("k")).get();
		//gravity
		double gravity = 10.0;
		double d       = pos.length();
		//gravitational force
		double gf      = 0.01 * k * gravity * d;
		disp = new VectorWritable( disp.getX()-(gf * pos.getX() / d), disp.getY()-(gf * pos.getY() / d));
		
		//limit the velocity
		disp = disp.divide(LIMIT);
		
		// limit
		double limitedDist = Math.min( (Math.sqrt(AREA)/10.0) * (1.0 / LIMIT), dispLength );
		disp = disp.divide(dispLength).multiply(limitedDist);

		// set new position
		pos = pos.add(disp);
		vertex.setValue(new VertexValueWritable(pos, new VectorWritable(0.0,0.0)));
	}

	
	/**
	* The function for calculating attractive force.
	* @param x the distance of two vertices
	* @return a double which is the attractive force
	*/
	private double fa(double x) {
		double k = ((DoubleWritable) aggregator.getAggregatedValue("k")).get();
		return x*x / k;
	}

	/**
	* The function for calculating repulsive force.
	* @param z the distance of two vertices
	* @return a double which is the repulsive force
	*/
	private double fr(double z) {
		double k = ((DoubleWritable) aggregator.getAggregatedValue("k")).get();
		return  k*k / z;
	}

	/**
	* Set an aggregator for tests.
	* @param aggregator the aggregator to set
	*/
	public void setAggregator(WorkerAggregatorUsage aggregator) {
		this.aggregator = aggregator;
	}
	

	
}