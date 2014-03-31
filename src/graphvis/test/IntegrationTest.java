/*
 * @(\#) IntegrationTest.java 1.1 10 March 20
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
package graphvis.test;

import static org.junit.Assert.*;

import org.apache.giraph.conf.BooleanConfOption;
import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.conf.IntConfOption;
import org.apache.giraph.utils.InternalVertexRunner;
import org.junit.Test;

import graphvis.engine.FruchtermanReingoldGraphVis;
import graphvis.io.CSVEdgeInputFormat;
import graphvis.io.GMLEdgeInputFormat;
import graphvis.io.GraphMLEdgeInputFormat;
import graphvis.io.SVGVertexOutputFormat;

/**
 * Contains integration tests for {@link FruchtermanReingoldGraphVis}
 * <p> 
 * @author Shan Huang 
 * @version 1.1 Initial development. 
 */
public class IntegrationTest {

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
		conf.setVertexOutputFormatClass(SVGVertexOutputFormat.class);
		 

		
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
		conf.setVertexOutputFormatClass(SVGVertexOutputFormat.class);
		conf.setMaxNumberOfSupersteps(10000);
		
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
		conf.setVertexOutputFormatClass(SVGVertexOutputFormat.class);
		conf.setMaxNumberOfSupersteps(10000);
		
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
