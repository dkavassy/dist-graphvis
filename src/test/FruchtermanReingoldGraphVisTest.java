/*
 * @(\#) FruchtermanReingoldGraphVisTest.java 1.1 10 March 20
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

package test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.edge.EdgeFactory;
import org.apache.giraph.graph.DefaultVertex;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.utils.InternalVertexRunner;
import org.apache.giraph.utils.MockUtils;
import org.apache.hadoop.io.*;
import org.junit.Test;

import engine.*;

/**
 * Contains unit tests for {@link FruchtermanReingoldGraphVis}
 * <p> 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */
public class FruchtermanReingoldGraphVisTest {

	/**
	 * Test superstep0
	 */
	@Test
	public void testSuperstep0() throws Exception {
	
		Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex = 
				new DefaultVertex<IntWritable, VertexValueWritable, EdgeValueWritable>();
		FruchtermanReingoldGraphVis computation = new FruchtermanReingoldGraphVis();
		double tBefore = FruchtermanReingoldGraphVis.getT();
		MockUtils.MockedEnvironment<IntWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> env = 
				MockUtils.prepareVertexAndComputation(vertex, new IntWritable(1),
						new VertexValueWritable(), false,
						computation, 0L);
		vertex.setValue(new VertexValueWritable());
		vertex.addEdge(EdgeFactory.create(new IntWritable(2),
				new EdgeValueWritable()));
		vertex.addEdge(EdgeFactory.create(new IntWritable(3),
				new EdgeValueWritable()));

		
		computation.compute(vertex, new ArrayList<MessageWritable>());
		double tAfter = FruchtermanReingoldGraphVis.getT();
		
		assertTrue(vertex.isHalted());
		//cool should not be called in superstep0
		assertEquals(0,tBefore-tAfter,0);
		//random positions should be between -500 and 500
		assertEquals(0,vertex.getValue().getPos().getX(),500);
		assertEquals(0,vertex.getValue().getPos().getY(),500);
		
	}
	
	/**
	 * Test the messages after finishes superstep1
	 */
	@Test
	public void testSuperstep1() throws Exception {
		//messages: positions of other vertices
		ArrayList<MessageWritable> messages = new ArrayList<MessageWritable>();
		messages.add(new MessageWritable(new IntWritable(2),new CoordinatesWritable(545,234)));
		messages.add(new MessageWritable(new IntWritable(3),new CoordinatesWritable(242,677)));
		Vertex<IntWritable, VertexValueWritable, EdgeValueWritable> vertex = 
				new DefaultVertex<IntWritable, VertexValueWritable, EdgeValueWritable>();
		FruchtermanReingoldGraphVis computation = new FruchtermanReingoldGraphVis();
		MockUtils.MockedEnvironment<IntWritable, VertexValueWritable, EdgeValueWritable, MessageWritable> env = 
				MockUtils.prepareVertexAndComputation(vertex, new IntWritable(1),
						new VertexValueWritable(), false,
						computation, 1L);
		//Mockito.when(SOURCE_ID.get(env.getConfiguration())).thenReturn(2L);
		vertex.setValue(new VertexValueWritable(new CoordinatesWritable(532,542),new CoordinatesWritable(322,445)));
		vertex.addEdge(EdgeFactory.create(new IntWritable(2),
				new EdgeValueWritable()));
		vertex.addEdge(EdgeFactory.create(new IntWritable(3),
				new EdgeValueWritable()));

		
		computation.compute(vertex, messages);

		assertTrue(vertex.isHalted());
		env.verifyMessageSent(new IntWritable(2), new MessageWritable(new IntWritable(1),vertex.getValue().getPos()));
		env.verifyMessageSent(new IntWritable(3), new MessageWritable(new IntWritable(1),vertex.getValue().getPos()));
	}

	
}
