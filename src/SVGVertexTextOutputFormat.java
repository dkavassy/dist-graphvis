import java.io.IOException;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
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
@SuppressWarnings("rawtypes")
public class SVGVertexTextOutputFormat<I extends WritableComparable, V extends VertexValueWritable, E extends EdgeValueWritable>
	extends TextVertexOutputFormat<I, V, E> 
{

  /** Specify the output delimiter */
  public static final String LINE_TOKENIZE_VALUE = "output.delimiter";
  /** Default output delimiter */
  public static final String LINE_TOKENIZE_VALUE_DEFAULT = "\t";
  /** Reverse id and value order? */
  //public static final String REVERSE_ID_AND_VALUE = "reverse.id.and.value";
  /** Default is to not reverse id and value order. */
  //public static final boolean REVERSE_ID_AND_VALUE_DEFAULT = false;

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
    private boolean reverseOutput;

    @Override
    public void initialize(TaskAttemptContext context) throws IOException,InterruptedException 
    {
      super.initialize(context);
      delimiter = getConf().get(LINE_TOKENIZE_VALUE, LINE_TOKENIZE_VALUE_DEFAULT);
      //reverseOutput = getConf().getBoolean( REVERSE_ID_AND_VALUE, REVERSE_ID_AND_VALUE_DEFAULT);
    }

    @Override
    protected Text convertVertexToLine(Vertex<I, V, E> vertex) throws IOException 
    {
    	long x = (long) vertex.getValue().getPos().getX();
    	long y = (long) vertex.getValue().getPos().getY();
    	
    	StringBuilder str = new StringBuilder();
    	
	    str.append(delimiter);
    	
    	// Create input svg style.
    	for (Edge edge : vertex.getEdges()) {
    		
    		long x2 = (long) (((EdgeValueWritable)edge.getValue()).getTargetPos().getX()+500);
    		long y2 = (long) (((EdgeValueWritable)edge.getValue()).getTargetPos().getY()+500);
    		str.append("<line x1=\"" + (x+500) + "\" y1=\"" + (y+500)
    				+ "\" x2=\"" + x2
    				+ "\" y2=\""
    				+ y2
    				+ "\" stroke=\"blue\" stroke-width=\"0.05\" />");
    		
    		str.append("<circle cx=\""
    				+ x2 + "\" cy=\""
    				+ y2
    				+"\" r=\"10\" stroke=\"red\" stroke-width=\"1\" fill=\"red\" />");
		}
    	
    	str.append("<circle cx=\""+ (x+500) + "\" cy=\""+ (y+500) +"\" r=\"10\" stroke=\"red\" stroke-width=\"1\" fill=\"red\" />");
    	
 	   
	  return new Text(str.toString());
    }
  }
}