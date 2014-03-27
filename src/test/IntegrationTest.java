package test;

import static org.junit.Assert.*;

import org.apache.giraph.conf.GiraphConfiguration;
import org.apache.giraph.utils.InternalVertexRunner;
import org.junit.Test;

import engine.CSVEdgeInputFormat;
import engine.FruchtermanReingoldGraphVis;
import engine.GMLEdgeInputFormat;
import engine.GraphMLEdgeInputFormat;
import engine.SVGVertexTextOutputFormat;

/**
 * Contains integration tests for {@link FruchtermanReingoldGraphVis}
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
