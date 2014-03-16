import java.io.IOException;

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;

public class GraphVis extends BasicComputation<IntWritable, CoordinatesPairWritable, NullWritable, CoordinatesWritable> {



	@Override
	public void compute(
			Vertex<IntWritable, CoordinatesPairWritable, NullWritable> vertex,
			Iterable<CoordinatesWritable> arg1) throws IOException {
		if (getSuperstep() == 0) {
			 CoordinatesWritable pos = CoordinatesWritableFactory.create();
			 pos.set( (long) (Math.random()*1000), (long) (Math.random()*1000) );
			 CoordinatesWritable disp = CoordinatesWritableFactory.create();
			 
			 CoordinatesPairWritable coords = CoordinatesPairWritableFactory.create();
			 coords.set(pos, disp);
			 
		     vertex.setValue(coords);
		 }

		 vertex.voteToHalt();
		
	}
}