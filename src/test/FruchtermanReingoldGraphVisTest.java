/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Contains a simple unit test for {@link FruchtermanReingoldGraphVis}
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
