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
	 * Test the behavior when a shorter path to a vertex has been found
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
		//assertTrue(!vertex.getValue().getPos().equals(new CoordinatesWritable(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)));
		env.verifyMessageSent(new IntWritable(2), new MessageWritable(new IntWritable(1),vertex.getValue().getPos()));
		env.verifyMessageSent(new IntWritable(3), new MessageWritable(new IntWritable(1),vertex.getValue().getPos()));
	}

	/**
	 * A local integration test for CSVEdgeInputFormat
	 */
	@Test
	public void testToyDataCSVEdgeInputFormat() throws Exception {

		// A small graph
		String[] graph = new String[] { "1,2", "1,3", "1,4", "2,3", "2,4",
				"3,4" };

		GiraphConfiguration conf = new GiraphConfiguration();
		conf.setComputationClass(FruchtermanReingoldGraphVis.class);
		conf.setEdgeInputFormatClass(CSVEdgeInputFormat.class);
		conf.setVertexOutputFormatClass(SVGVertexTextOutputFormat.class);

		// Run internally
		Iterable<String> results = InternalVertexRunner.run(conf, null, graph);
		String output="";
		for(String result:results){
			output+=result;
		}
		System.out.println(output);
        //count lines and circles in output
        int lineCount = 0;
        int circleCount = 0;
        String l = "line";
        String c = "circle";
        int result = output.indexOf(l);
        while(result !=-1)
        {
        result = output.indexOf(l,result+1);

        lineCount++;
        }
        
        result = output.indexOf(c);
        while(result !=-1)
        {
        result = output.indexOf(c,result+1);

        circleCount++;
        }
        //contains 6 edges and 4 vertices
        assertEquals(6,lineCount);
        //print circles for each edge and for each vertex, so it's number of edges + number of edges
        assertEquals(6+4,circleCount);
	}
	
	/**
	 * A local integration test for GMLEdgeInputFormat
	 */
	@Test
	public void testToyDataGMLEdgeInputFormat() throws Exception {

		// A small graph
		String[] graph = new String[] { 
				"edge [ source 2 target 1 ]", 
				"edge [ source 3 target 1 ]", 
				"edge [ source 4 target 1 ]"
				};

		GiraphConfiguration conf = new GiraphConfiguration();
		conf.setComputationClass(FruchtermanReingoldGraphVis.class);
		conf.setEdgeInputFormatClass(GMLEdgeInputFormat.class);
		conf.setVertexOutputFormatClass(SVGVertexTextOutputFormat.class);

		// Run internally
		Iterable<String> results = InternalVertexRunner.run(conf, null, graph);
		String output="";
		for(String result:results){
			output+=result;
		}
		System.out.println(output);
        //count lines and circles in output
        int lineCount = 0;
        int circleCount = 0;
        String l = "line";
        String c = "circle";
        int result = output.indexOf(l);
        while(result !=-1)
        {
        result = output.indexOf(l,result+1);

        lineCount++;
        }
        
        result = output.indexOf(c);
        while(result !=-1)
        {
        result = output.indexOf(c,result+1);

        circleCount++;
        }
        //contains 6 edges and 4 vertices
        assertEquals(3,lineCount);
        //print circles for each edge and for each vertex, so it's number of edges + number of edges
        assertEquals(3+4,circleCount);
	}

	
	/**
	 * A local integration test for GraphMLEdgeInputFormat
	 */
	@Test
	public void testToyDataGraphMLEdgeInputFormat() throws Exception {

		// A small graph
		String[] graph = new String[] { 
				"<edge id=\""+1+"\" source=\""+2+"\" target=\""+1+"\">", 
				"<edge id=\""+2+"\" source=\""+3+"\" target=\""+1+"\"/>", 
				"<edge id=\""+3+"\" source=\""+4+"\" target=\""+1+"\"/>"
				};

		GiraphConfiguration conf = new GiraphConfiguration();
		conf.setComputationClass(FruchtermanReingoldGraphVis.class);
		conf.setEdgeInputFormatClass(GraphMLEdgeInputFormat.class);
		conf.setVertexOutputFormatClass(SVGVertexTextOutputFormat.class);

		// Run internally
		Iterable<String> results = InternalVertexRunner.run(conf, null, graph);
		String output="";
		for(String result:results){
			output+=result;
		}
		System.out.println(output);
        //count lines and circles in output
        int lineCount = 0;
        int circleCount = 0;
        String l = "line";
        String c = "circle";
        int result = output.indexOf(l);
        while(result !=-1)
        {
        result = output.indexOf(l,result+1);

        lineCount++;
        }
        
        result = output.indexOf(c);
        while(result !=-1)
        {
        result = output.indexOf(c,result+1);

        circleCount++;
        }
        //contains 6 edges and 4 vertices
        assertEquals(3,lineCount);
        //print circles for each edge and for each vertex, so it's number of edges + number of edges
        assertEquals(3+4,circleCount);
	}
}
