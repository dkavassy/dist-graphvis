/*
 * @(\#) SomeClass.java 1.1 10 February 14
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

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
* A class that write vertices and edges to an SVG file.
* <p>
* 
* @author James Pierce
* @version 1.1 Initial development.
*/
public class SVGVertexTextOutputFormat<I extends IntWritable, V extends VertexValueWritable, E extends EdgeValueWritable>
	extends TextVertexOutputFormat<I, V, E> 
{

  /** Specify the output delimiter */
  public static final String LINE_TOKENIZE_VALUE = "output.delimiter";
  /** Default output delimiter */
  public static final String LINE_TOKENIZE_VALUE_DEFAULT = "\t";
  
  public static final double W = 100.0, L = 100.0;

  @Override
  public TextVertexWriter createVertexWriter(TaskAttemptContext context) 
  {
    return new SVGVertexWriter();
  }

  protected class SVGVertexWriter extends TextVertexWriterToEachLine 
  {
    /** Saved delimiter */
    private String delimiter;
    /** Cached reserve option */

    @Override
    public void initialize(TaskAttemptContext context) throws IOException,InterruptedException 
    {
      super.initialize(context);
      delimiter = getConf().get(LINE_TOKENIZE_VALUE, LINE_TOKENIZE_VALUE_DEFAULT);
    }

    @Override
    protected Text convertVertexToLine(Vertex<I, V, E> vertex) throws IOException 
    {
    	long x = (long) (vertex.getValue().getPos().getX()+W/2);
    	long y = (long) (vertex.getValue().getPos().getY()+L/2);
    	
    	StringBuilder str = new StringBuilder();
    	
	    str.append(delimiter);
    	
    	// Create input svg style.
    	for (Edge<I, E> edge : vertex.getEdges()) {
    		
    		long x2 = (long) (((EdgeValueWritable)edge.getValue()).getTargetPos().getX()+W/2);
    		long y2 = (long) (((EdgeValueWritable)edge.getValue()).getTargetPos().getY()+L/2);
    		str.append("<line x1=\"" + x + "\" y1=\"" + y
    				+ "\" x2=\"" + x2
    				+ "\" y2=\"" + y2
    				+ "\" stroke=\"blue\" stroke-width=\"0.5\" />");
    		
    		str.append("<circle cx=\""
    				+ x2 + "\" cy=\"" + y2
    				+"\" r=\"1\" stroke=\"red\" stroke-width=\"1\" fill=\"red\" />");
		}
    	
    	str.append("<circle cx=\""
    				+ x + "\" cy=\""
    				+ y +"\" r=\"1\" stroke=\"red\" stroke-width=\"1\" fill=\"red\" />");
    	
 	   
	  return new Text(str.toString());
    }
  }
}