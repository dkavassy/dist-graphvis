package engine;
import java.io.IOException;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
* Write out Vertices' IDs and values, but not their edges nor edges' values.
* This is a useful output format when the final value of the vertex is
* all that's needed. The boolean configuration parameter reverse.id.and.value
* allows reversing the output of id and value.
*
* @param <I> Vertex index value
* @param <V> Vertex value
* @param <E> Edge value
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

  /**
* Vertex writer used with {@link IdWithValueTextOutputFormat}.
*/
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