import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.giraph.utils.IntPair;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class CSVEdgeInputFormat extends TextEdgeInputFormat<IntWritable, EdgeValueTypeWritable>{

	@Override
	public EdgeReader<IntWritable, EdgeValueTypeWritable> createEdgeReader(
			InputSplit arg0, TaskAttemptContext arg1) throws IOException {
		return new CSVEdgeReader();
	}

	
	/**
	* {@link org.apache.giraph.io.EdgeReader} associated with
	* {@link IntNullTextEdgeInputFormat}.
	*/
	  public class CSVEdgeReader extends
	      TextEdgeReaderFromEachLineProcessed<IntPair> {
		  private final Pattern separator = Pattern.compile("[,]");
	    @Override
	    protected IntPair preprocessLine(Text line) throws IOException {
	      String[] tokens = separator.split(line.toString());
	      return new IntPair(Integer.valueOf(tokens[0]),
	          Integer.valueOf(tokens[1]));
	    }

	    @Override
	    protected IntWritable getSourceVertexId(IntPair endpoints)
	      throws IOException {
	      return new IntWritable(endpoints.getFirst());
	    }

	    @Override
	    protected IntWritable getTargetVertexId(IntPair endpoints)
	      throws IOException {
	      return new IntWritable(endpoints.getSecond());
	    }

	    @Override
	    protected EdgeValueTypeWritable getValue(IntPair endpoints) throws IOException {
	      return new EdgeValueTypeWritable(new LongWritable(0L), new CoordinatesWritable());
	    }
	  }
}
