package engine;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.giraph.utils.IntPair;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class GraphMLEdgeInputFormat extends TextEdgeInputFormat<IntWritable, EdgeValueWritable> {

	@Override
	public EdgeReader<IntWritable, EdgeValueWritable> createEdgeReader(
			InputSplit arg0, TaskAttemptContext arg1) throws IOException {
		
		return new GraphMLEdgeReader();
	}

	
	public class GraphMLEdgeReader extends
	TextEdgeReaderFromEachLineProcessed<IntPair> {
		private final Pattern separator = Pattern.compile("[\\s]");
		
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
		protected EdgeValueWritable getValue(IntPair endpoints) throws IOException {
			return new EdgeValueWritable(new LongWritable(0L), new CoordinatesWritable());
		}

		@Override
		protected IntPair preprocessLine(Text text) throws IOException {
			String line = text.toString().split("[<>]")[1];
			String[] tokens = separator.split(line);
			return new IntPair(Integer.parseInt(tokens[2].split("[\"]")[1]),
					Integer.parseInt(tokens[3].split("[\"]")[1]));
		}
		
		
		
		
		
		
		
		
	}

}
